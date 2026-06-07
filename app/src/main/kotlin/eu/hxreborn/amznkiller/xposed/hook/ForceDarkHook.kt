package eu.hxreborn.amznkiller.xposed.hook

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import java.util.Collections
import java.util.WeakHashMap

object ForceDarkHook {
    val bottomTabIcons: MutableSet<View> =
        Collections.synchronizedSet(
            Collections.newSetFromMap(WeakHashMap()),
        )

    // GPU force dark inverts this grey to near-white
    private val TAB_ICON_TINT = Color.rgb(168, 168, 168)
    private val TAB_ICON_CSL = ColorStateList.valueOf(TAB_ICON_TINT)

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        Logger.debug {
            "dark probe device=${Build.MANUFACTURER}/${Build.MODEL} " +
                "sdk=${Build.VERSION.SDK_INT} hw=${Build.HARDWARE}"
        }
        hookActivityOnCreate(xposed)
        hookDetermineForceDarkType(xposed)
        hookRendererSetForceDark(xposed)
        hookWebViewBackground(xposed)
        hookTabIcons(xposed, classLoader)
    }

    fun applyTabIconTint(imageView: ImageView) {
        bottomTabIcons.add(imageView)
        imageView.imageTintList = TAB_ICON_CSL
        imageView.colorFilter = PorterDuffColorFilter(TAB_ICON_TINT, PorterDuff.Mode.SRC_IN)
        Logger.debug { "dark tint apply view=${imageView.hashCode()}" }
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
            if (!forceDarkWebview) return@hookMethod chain.proceed()
            if (chain.thisObject !is WebView) return@hookMethod chain.proceed()
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

    private fun hookTabIcons(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val controllers =
            listOf(
                "com.amazon.mShop.chrome.bottomtabs.BaseTabController",
                "com.amazon.mShop.chrome.bottomtabs.SavXTabController",
                "com.amazon.mShop.chrome.bottomtabs.SwitcherTabController",
            )
        var hooked = 0
        for (cls in controllers) {
            runCatching {
                val clazz = Class.forName(cls, false, classLoader)
                xposed
                    .hook(clazz.declaredMethods.first { it.name == "getTabIcon" })
                    .intercept { chain ->
                        val result = chain.proceed()
                        if (!forceDarkWebview) return@intercept result
                        val icon = result as? ImageView ?: return@intercept result
                        Logger.debug { "dark tab icon class=${icon.javaClass.name} id=${icon.id}" }
                        applyTabIconTint(icon)
                        result
                    }
            }.onSuccess {
                hooked++
                Logger.debug { "hooked target=$cls.getTabIcon" }
            }.onFailure {
                Logger.debug { "hook fail target=$cls.getTabIcon msg=${it.message}" }
            }
        }
        Logger.debug { "dark tab icons hooked=$hooked total=${controllers.size}" }
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageDrawable",
            Drawable::class.java,
        ) { chain ->
            chain.proceed()
            if (!forceDarkWebview) return@hookMethod null
            val iv = chain.thisObject as? ImageView ?: return@hookMethod null
            if (iv !in bottomTabIcons) return@hookMethod null
            Logger.debug { "dark tab redraw view=${iv.hashCode()}" }
            iv.post { applyTabIconTint(iv) }
            null
        }
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageTintList",
            ColorStateList::class.java,
        ) { chain ->
            if (!forceDarkWebview) return@hookMethod chain.proceed()
            val iv = chain.thisObject as? ImageView ?: return@hookMethod chain.proceed()
            if (iv !in bottomTabIcons) return@hookMethod chain.proceed()
            Logger.debug { "dark tab tint guard view=${iv.hashCode()}" }
            chain.proceed(arrayOf(ColorStateList.valueOf(Color.rgb(168, 168, 168))))
        }
    }
}
