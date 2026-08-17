package eu.hxreborn.amznkiller.xposed.hook

import android.app.Activity
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.descendants
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

object ForceDarkHook {
    private const val BOTTOM_TABS_BAR = "com.amazon.mShop.rendering.BottomTabsBarV2"
    private const val BASE_TAB_CONTROLLER = "com.amazon.mShop.chrome.bottomtabs.BaseTabController"
    private const val NAVIGABLE = "com.amazon.platform.navigation.api.state.Navigable"
    private const val UI_RENDERING_MODE = "com.amazon.mShop.chrome.UiRenderingMode"
    private const val METHOD_BAR_VIEW = "getBottomTabsView"
    private const val METHOD_PAGE_TYPE_GATE = "isDarkModeEnabledByPageType"
    private const val METHOD_APPLY_BACKGROUND = "applyBottomTabsBackgroundResources"
    private const val METHOD_SWITCH_COLOR_MODE = "switchColorMode"
    private const val METHOD_UPDATE_TAB_ITEM = "updateTabItemWithMode"
    private const val METHOD_UPDATE_UI = "updateUI"
    private const val FIELD_TAB_ICON = "mBottomTabIcon"
    private const val ENUM_DARK_MODE = "DarkMode"
    private const val TAB_ICON_ID_PREFIX = "bottom_tab_button_icon"

    private const val PROBE_DELAY_MS = 600L
    private const val EDGE_INSET = 8
    private const val SCAN_STEP = 2
    private const val MID_LUMINANCE = 128
    private const val LEGIBLE_BRIGHT_RATIO = 0.05f

    private val READABLE_ICON = Color.rgb(232, 232, 232)
    private val READABLE_TINT = ColorStateList.valueOf(READABLE_ICON)

    // Whether force dark reaches the bottom bar differs by device, and decides the whole approach
    private enum class Inversion { UNKNOWN, REACHES_BAR, SKIPS_BAR }

    private val probePending = AtomicBoolean(false)

    private val unreadableIcons: MutableSet<ImageView> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

    @Volatile
    private var inversion = Inversion.UNKNOWN

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        Logger.debug {
            "dark probe device=${Build.MANUFACTURER}/${Build.MODEL} " +
                "sdk=${Build.VERSION.SDK_INT} hw=${Build.HARDWARE}"
        }
        probeForceDarkType()
        hookActivityOnCreate(xposed)
        hookDetermineForceDarkType(xposed)
        hookRendererSetForceDark(xposed)
        hookWebViewBackground(xposed)
        hookBottomTabsDarkMode(xposed, classLoader)
    }

    private fun probeForceDarkType() {
        runCatching {
            val fields =
                Class
                    .forName("android.graphics.ForceDarkType")
                    .declaredFields
                    .filter { it.type == Int::class.javaPrimitiveType }
                    .joinToString(",") {
                        it.isAccessible = true
                        "${it.name}=${it.getInt(null)}"
                    }
            Logger.debug { "dark probe forcedarktype $fields" }
        }.onFailure {
            Logger.debug { "dark probe forcedarktype missing msg=${it.message}" }
        }
    }

    private fun hookMethod(
        xposed: XposedInterface,
        clazz: Class<*>,
        name: String,
        vararg params: Class<*>,
        interceptor: XposedInterface.Hooker,
    ) {
        runCatching {
            xposed.hook(clazz.getDeclaredMethod(name, *params)).intercept(interceptor)
        }.onSuccess {
            Logger.debug { "hooked target=${clazz.simpleName}.$name" }
        }.onFailure {
            Logger.log(Log.ERROR, "hook fail target=${clazz.simpleName}.$name", it)
        }
    }

    // Bar colors are no-ops under enforced edge to edge, still needed below API 35
    @Suppress("DEPRECATION")
    private fun hookActivityOnCreate(xposed: XposedInterface) {
        hookMethod(
            xposed,
            Activity::class.java,
            "onCreate",
            Bundle::class.java,
        ) { chain ->
            chain.proceed()
            if (!forceDarkWebview) return@hookMethod null
            val activity = chain.thisObject as? Activity ?: return@hookMethod null
            runCatching {
                val decor = activity.window?.decorView
                val before = decor?.isForceDarkAllowed
                decor?.isForceDarkAllowed = true
                activity.window?.setBackgroundDrawable(Color.BLACK.toDrawable())
                activity.window?.statusBarColor = Color.BLACK
                activity.window?.navigationBarColor = Color.BLACK
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.window?.insetsController?.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    )
                }
                val uiMode = activity.resources.configuration.uiMode
                val nightMode = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                Logger.debug {
                    "dark activity apply class=${activity.javaClass.name} " +
                        "prev=$before nightMode=$nightMode " +
                        "decor=${decor?.javaClass?.simpleName}"
                }
            }.onFailure {
                Logger.debug {
                    "dark activity fail class=${activity.javaClass.name} msg=${it.message}"
                }
            }
            null
        }
    }

    // Amazon sets android:forceDarkAllowed=false in AmazonTheme
    // Override determineForceDarkType result from 0 (NONE) to 2 (ALWAYS)
    private fun hookDetermineForceDarkType(xposed: XposedInterface) {
        runCatching {
            val clazz = Class.forName("android.view.ViewRootImpl")
            Logger.debug {
                val names =
                    clazz.declaredMethods
                        .filter {
                            "forcedark" in it.name.lowercase()
                        }.map { it.name }
                "dark probe vri-methods count=${names.size}"
            }
            xposed.hook(clazz.getDeclaredMethod("determineForceDarkType")).intercept { chain ->
                val result = chain.proceed()
                if (!forceDarkWebview) return@intercept result
                if (result !is Int) {
                    Logger.debug { "dark detect type unexpected class=${result?.javaClass?.name}" }
                    return@intercept result
                }
                if (result != 0) {
                    Logger.debug { "dark detect skip already=$result" }
                    return@intercept result
                }
                Logger.debug { "dark detect override prev=$result next=2" }
                2
            }
        }.onSuccess {
            Logger.debug { "hooked target=ViewRootImpl.determineForceDarkType" }
        }.onFailure {
            Logger.log(Log.ERROR, "hook fail target=ViewRootImpl.determineForceDarkType", it)
        }
    }

    // Fallback for older Android where determineForceDarkType doesn't exist
    private fun hookRendererSetForceDark(xposed: XposedInterface) {
        val classes =
            listOf(
                "android.graphics.HardwareRenderer",
                "android.view.ThreadedRenderer",
            )
        val params =
            listOf(
                Boolean::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            )
        for (cls in classes) {
            val clazz =
                runCatching { Class.forName(cls) }.getOrElse {
                    Logger.debug { "dark renderer skip class=$cls reason=not-found" }
                    continue
                }
            for (param in params) {
                val ok =
                    runCatching {
                        val method = clazz.getDeclaredMethod("setForceDark", param)
                        xposed.hook(method).intercept { chain ->
                            if (!forceDarkWebview) {
                                return@intercept chain.proceed()
                            }
                            when (val arg = chain.getArg(0)) {
                                is Boolean -> {
                                    if (!arg) {
                                        Logger.debug {
                                            "dark renderer override class=$cls arg=$arg next=true"
                                        }
                                        return@intercept chain.proceed(arrayOf(true))
                                    }
                                }

                                is Int -> {
                                    if (arg != 2) {
                                        Logger.debug {
                                            "dark renderer override class=$cls arg=$arg next=2"
                                        }
                                        return@intercept chain.proceed(arrayOf(2))
                                    }
                                }
                            }
                            chain.proceed()
                        }
                    }
                if (ok.isSuccess) {
                    Logger.debug { "hooked target=$cls.setForceDark param=${param.simpleName}" }
                    return
                } else {
                    Logger.debug {
                        "dark renderer skip class=$cls param=${param.simpleName} reason=not-found"
                    }
                }
            }
        }
        Logger.log(Log.WARN, "hook fail target=renderer.setForceDark reason=no-matching-class")
    }

    private fun hookWebViewBackground(xposed: XposedInterface) {
        var hookedCtors = 0
        for (ctor in WebView::class.java.declaredConstructors) {
            runCatching {
                xposed.hook(ctor).intercept { chain ->
                    chain.proceed()
                    if (!forceDarkWebview) return@intercept null
                    val webView = chain.thisObject as? WebView ?: return@intercept null
                    runCatching {
                        webView.setBackgroundColor(Color.TRANSPARENT)
                        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        Logger.debug {
                            "dark webview init class=${webView.javaClass.name} bg=transparent layer=hw"
                        }
                    }
                    null
                }
                hookedCtors++
            }.onFailure {
                Logger.debug { "hook fail target=WebView.<init> msg=${it.message}" }
            }
        }
        Logger.debug { "dark webview ctors hooked=$hookedCtors" }
        hookMethod(
            xposed,
            View::class.java,
            "setBackgroundColor",
            Int::class.javaPrimitiveType!!,
        ) { chain ->
            if (chain.thisObject !is WebView) return@hookMethod chain.proceed()
            if (!forceDarkWebview) return@hookMethod chain.proceed()
            val color = chain.getArg(0) as? Int ?: return@hookMethod chain.proceed()
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (r > 200 && g > 200 && b > 200) {
                Logger.debug {
                    "dark bg block color=#${Integer.toHexString(
                        color,
                    )} view=${(chain.thisObject as View).javaClass.name}"
                }
                chain.proceed(arrayOf(Color.TRANSPARENT))
            } else {
                Logger.debug { "dark bg pass color=#${Integer.toHexString(color)}" }
                chain.proceed()
            }
        }
    }

    // Amazon gates its own bottom bar dark mode to the Alexa companion page type. Opening that
    // gate is only correct where force dark leaves the bar alone, so read the drawn bar first
    private fun hookBottomTabsDarkMode(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val barClass = classLoader.loadOrNull(BOTTOM_TABS_BAR) ?: return
        val navigable = classLoader.loadOrNull(NAVIGABLE) ?: return
        val barView =
            runCatching { barClass.getMethod(METHOD_BAR_VIEW) }.getOrElse {
                Logger.log(Log.ERROR, "hook fail target=$BOTTOM_TABS_BAR.$METHOD_BAR_VIEW", it)
                return
            }
        hookMethod(xposed, barClass, METHOD_PAGE_TYPE_GATE, String::class.java) { chain ->
            if (forceDarkWebview && inversion == Inversion.SKIPS_BAR) true else chain.proceed()
        }
        // The gate is a one liner, so ART can inline it into its only caller and never reach the hook
        runCatching {
            xposed.deoptimize(barClass.declaredMethods.first { it.name == METHOD_UPDATE_UI })
        }.onFailure {
            Logger.debug { "dark tab bar deoptimize skip msg=${it.message}" }
        }
        hookMethod(xposed, barClass, METHOD_APPLY_BACKGROUND, navigable) { chain ->
            val result = chain.proceed()
            if (!forceDarkWebview || inversion != Inversion.UNKNOWN) return@hookMethod result
            val owner = chain.thisObject ?: return@hookMethod result
            val bar = runCatching { barView.invoke(owner) as? View }.getOrNull()
            bar?.let { scheduleProbe(it) { applyDarkMode(owner, it) } }
            result
        }
        hookTabIconRetint(xposed, classLoader)
    }

    // The bar has to be laid out and drawn before it can be read back
    private fun scheduleProbe(
        bar: View,
        onSkipsBar: () -> Unit,
    ) {
        if (!probePending.compareAndSet(false, true)) return
        bar.postDelayed({
            probePending.set(false)
            if (inversion == Inversion.UNKNOWN) probeBar(bar, onSkipsBar)
        }, PROBE_DELAY_MS)
    }

    // Amazon still paints the bar light here, so a dark readback means force dark reached it
    private fun probeBar(
        bar: View,
        onSkipsBar: () -> Unit,
    ) {
        val window = bar.activity?.window
        val handler = bar.handler
        if (window == null || handler == null || bar.width <= 0 || bar.height <= 0) {
            Logger.debug {
                "dark tab bar probe skip window=${window != null} size=${bar.width}x${bar.height}"
            }
            return
        }
        val origin = bar.locationInWindow
        val shot = Bitmap.createBitmap(bar.width, bar.height, Bitmap.Config.ARGB_8888)
        val rect = Rect(origin.x, origin.y, origin.x + bar.width, origin.y + bar.height)
        PixelCopy.request(window, rect, shot, { status ->
            if (status != PixelCopy.SUCCESS) {
                Logger.debug { "dark tab bar probe status=$status" }
                return@request
            }
            val barLuminance = luminance(shot.getPixel(EDGE_INSET, shot.height / 2))
            inversion =
                if (barLuminance < MID_LUMINANCE) Inversion.REACHES_BAR else Inversion.SKIPS_BAR
            Logger.debug { "dark tab bar probe luminance=$barLuminance result=$inversion" }
            if (inversion ==
                Inversion.SKIPS_BAR
            ) {
                onSkipsBar()
            } else {
                lightenUnreadableIcons(bar, shot, origin)
            }
        }, handler)
    }

    // Force dark leaves real artwork alone, so an icon shipped as a bitmap stays dark on a dark bar
    private fun lightenUnreadableIcons(
        bar: View,
        shot: Bitmap,
        origin: Point,
    ) {
        bar.tabIcons().filterNot { it.isLegibleIn(shot, origin) }.forEach { icon ->
            unreadableIcons += icon
            icon.applyReadableTint()
            Logger.debug { "dark tab icon lighten id=${icon.idName}" }
        }
    }

    // A legible glyph paints a good share of its own box bright, whatever a sibling badge does
    private fun ImageView.isLegibleIn(
        shot: Bitmap,
        origin: Point,
    ): Boolean {
        val at = locationInWindow
        var bright = 0
        var scanned = 0
        for (x in at.x - origin.x until at.x - origin.x + width step SCAN_STEP) {
            for (y in at.y - origin.y until at.y - origin.y + height step SCAN_STEP) {
                if (x !in 0 until shot.width || y !in 0 until shot.height) continue
                scanned++
                if (luminance(shot.getPixel(x, y)) >= MID_LUMINANCE) bright++
            }
        }
        return scanned == 0 || bright.toFloat() / scanned >= LEGIBLE_BRIGHT_RATIO
    }

    // A color filter survives what force dark does to a tint, so the glyph stays readable either way
    private fun ImageView.applyReadableTint() {
        imageTintList = READABLE_TINT
        colorFilter = PorterDuffColorFilter(READABLE_ICON, PorterDuff.Mode.SRC_IN)
    }

    // Amazon repaints its icons whenever a tab reloads, which drops the filter we put there
    private fun hookTabIconRetint(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val controller = classLoader.loadOrNull(BASE_TAB_CONTROLLER) ?: return
        val iconField =
            runCatching {
                controller.getDeclaredField(FIELD_TAB_ICON).apply { isAccessible = true }
            }.getOrElse {
                Logger.log(Log.ERROR, "hook fail target=$BASE_TAB_CONTROLLER.$FIELD_TAB_ICON", it)
                return
            }
        runCatching {
            val method = controller.declaredMethods.first { it.name == METHOD_UPDATE_TAB_ITEM }
            xposed.hook(method).intercept { chain ->
                val result = chain.proceed()
                if (!forceDarkWebview) return@intercept result
                val icon = runCatching { iconField.get(chain.thisObject) as? ImageView }.getOrNull()
                if (icon != null && icon in unreadableIcons) icon.applyReadableTint()
                result
            }
        }.onSuccess {
            Logger.debug { "hooked target=$BASE_TAB_CONTROLLER.$METHOD_UPDATE_TAB_ITEM" }
        }.onFailure {
            Logger.log(
                Log.ERROR,
                "hook fail target=$BASE_TAB_CONTROLLER.$METHOD_UPDATE_TAB_ITEM",
                it,
            )
        }
    }

    // Amazon only repaints on navigation, so ask for the dark pass now that the probe called for it
    private fun applyDarkMode(
        owner: Any,
        bar: View,
    ) {
        runCatching {
            val modes = owner.javaClass.classLoader?.loadOrNull(UI_RENDERING_MODE) ?: return
            val darkMode = modes.enumConstants?.first { (it as Enum<*>).name == ENUM_DARK_MODE }
            owner.javaClass.declaredMethods
                .first { it.name == METHOD_SWITCH_COLOR_MODE }
                .apply { isAccessible = true }
                .invoke(owner, darkMode, null)
            bar.invalidate()
            Logger.debug { "dark tab bar dark mode applied" }
        }.onFailure {
            Logger.log(Log.ERROR, "dark tab bar dark mode fail", it)
        }
    }

    private fun ClassLoader.loadOrNull(name: String): Class<*>? =
        runCatching { Class.forName(name, false, this) }
            .onFailure {
                Logger.log(
                    Log.ERROR,
                    "hook fail target=$name reason=class-not-found",
                    it,
                )
            }.getOrNull()

    private fun View.tabIcons(): Sequence<ImageView> =
        ((this as? ViewGroup)?.descendants ?: emptySequence())
            .filterIsInstance<ImageView>()
            .filter { it.width > 0 && it.drawable != null }
            .filter { it.idName?.startsWith(TAB_ICON_ID_PREFIX) == true }

    private val View.idName: String?
        get() = runCatching { resources.getResourceEntryName(id) }.getOrNull()

    private val View.locationInWindow: Point
        get() = IntArray(2).also { getLocationInWindow(it) }.let { Point(it[0], it[1]) }

    private val View.activity: Activity?
        get() =
            generateSequence(context) { (it as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>()
                .firstOrNull()

    private fun luminance(color: Int): Int =
        (Color.red(color) * 2 + Color.green(color) * 5 + Color.blue(color)) / 8
}
