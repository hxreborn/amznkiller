package eu.hxreborn.amznkiller.xposed.hook

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.ImageView
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import java.util.Collections
import java.util.WeakHashMap

object ForceDarkHooker {
    val bottomTabIcons: MutableSet<View> = Collections.newSetFromMap(WeakHashMap())

    // GPU force dark inverts this grey to near-white
    private val TAB_ICON_TINT = Color.rgb(168, 168, 168)
    private val TAB_ICON_CSL = ColorStateList.valueOf(TAB_ICON_TINT)

    lateinit var hostClassLoader: ClassLoader

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        hostClassLoader = classLoader
        hookActivityOnCreate(xposed)
        hookDetermineForceDarkType(xposed)
        hookRendererSetForceDark(xposed)
        hookWebViewBackground(xposed)
        hookTabIcons(xposed)
    }

    fun applyTabIconTint(imageView: ImageView) {
        bottomTabIcons.add(imageView)
        imageView.imageTintList = TAB_ICON_CSL
        imageView.colorFilter = PorterDuffColorFilter(TAB_ICON_TINT, PorterDuff.Mode.SRC_IN)
        Logger.logDebug("ForceDark: applyTabIconTint view=${imageView.hashCode()}")
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
            Logger.log("Hooked ${clazz.simpleName}.$name")
        }.onFailure {
            Logger.log("Failed to hook ${clazz.simpleName}.$name", it)
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
            if (!PrefsManager.forceDarkWebview) return@hookMethod null
            val activity = chain.thisObject as? Activity ?: return@hookMethod null
            runCatching {
                activity.window?.decorView?.let { it.isForceDarkAllowed = true }
                activity.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
                activity.window?.statusBarColor = Color.BLACK
                activity.window?.navigationBarColor = Color.BLACK
                activity.window?.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                )
                val fda = activity.window?.decorView?.isForceDarkAllowed
                Logger.logDebug("ForceDark: onCreate forceDarkAllowed=$fda")
            }
            null
        }
    }

    // Amazon sets android:forceDarkAllowed=false in AmazonTheme
    // Override determineForceDarkType result from 0 (NONE) to 2 (ALWAYS)
    private fun hookDetermineForceDarkType(xposed: XposedInterface) {
        runCatching {
            val clazz = Class.forName("android.view.ViewRootImpl")
            Logger.logDebug(
                "ForceDark: ViewRootImpl forceDark methods: ${
                    clazz.declaredMethods.filter { "forcedark" in it.name.lowercase() }
                        .map { it.name }
                }",
            )
            xposed.hook(clazz.getDeclaredMethod("determineForceDarkType")).intercept { chain ->
                val result = chain.proceed()
                if (!PrefsManager.forceDarkWebview || result !is Int || result != 0) {
                    return@intercept result
                }
                Logger.logDebug("ForceDark: determineForceDarkType $result -> 2")
                2
            }
        }.onSuccess {
            Logger.log("Hooked ViewRootImpl.determineForceDarkType")
        }.onFailure {
            Logger.log("Failed to hook determineForceDarkType", it)
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
            val clazz = runCatching { Class.forName(cls) }.getOrNull() ?: continue
            for (param in params) {
                val ok =
                    runCatching {
                        xposed
                            .hook(clazz.getDeclaredMethod("setForceDark", param))
                            .intercept { chain ->
                                if (!PrefsManager.forceDarkWebview) {
                                    return@intercept chain.proceed()
                                }
                                when (val arg = chain.getArg(0)) {
                                    is Boolean -> {
                                        if (!arg) {
                                            return@intercept chain.proceed(arrayOf(true))
                                        }
                                    }

                                    is Int -> {
                                        if (arg != 2) {
                                            return@intercept chain.proceed(arrayOf(2))
                                        }
                                    }
                                }
                                chain.proceed()
                            }
                    }
                if (ok.isSuccess) {
                    Logger.log("Hooked $cls.setForceDark(${param.simpleName})")
                    return
                }
            }
        }
        Logger.log("Failed to hook setForceDark on any renderer class")
    }

    private fun hookWebViewBackground(xposed: XposedInterface) {
        for (ctor in WebView::class.java.declaredConstructors) {
            runCatching {
                xposed.hook(ctor).intercept { chain ->
                    chain.proceed()
                    if (!PrefsManager.forceDarkWebview) return@intercept null
                    val webView = chain.thisObject as? WebView ?: return@intercept null
                    runCatching {
                        webView.setBackgroundColor(Color.TRANSPARENT)
                        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        Logger.logDebug(
                            "ForceDark: WebView ctor, set transparent bg + HW layer",
                        )
                    }
                    null
                }
            }
        }
        hookMethod(
            xposed,
            View::class.java,
            "setBackgroundColor",
            Int::class.javaPrimitiveType!!,
        ) { chain ->
            if (!PrefsManager.forceDarkWebview) return@hookMethod chain.proceed()
            if (chain.thisObject !is WebView) return@hookMethod chain.proceed()
            val color = chain.getArg(0) as? Int ?: return@hookMethod chain.proceed()
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (r > 200 && g > 200 && b > 200) {
                Logger.logDebug("ForceDark: blocked bg #${Integer.toHexString(color)}")
                chain.proceed(arrayOf(Color.TRANSPARENT))
            } else {
                chain.proceed()
            }
        }
    }

    private fun hookTabIcons(xposed: XposedInterface) {
        val controllers =
            listOf(
                "com.amazon.mShop.chrome.bottomtabs.BaseTabController",
                "com.amazon.mShop.chrome.bottomtabs.SavXTabController",
                "com.amazon.mShop.chrome.bottomtabs.SwitcherTabController",
            )
        var hooked = 0
        for (cls in controllers) {
            runCatching {
                val clazz = Class.forName(cls, false, hostClassLoader)
                xposed
                    .hook(clazz.declaredMethods.first { it.name == "getTabIcon" })
                    .intercept { chain ->
                        val result = chain.proceed()
                        if (!PrefsManager.forceDarkWebview) return@intercept result
                        val icon = result as? ImageView ?: return@intercept result
                        Logger.logDebug(
                            "ForceDark: getTabIcon ${icon.javaClass.name} id=${icon.id}",
                        )
                        applyTabIconTint(icon)
                        result
                    }
            }.onSuccess {
                hooked++
                Logger.log("Hooked $cls.getTabIcon")
            }.onFailure {
                Logger.logDebug("Failed to hook $cls.getTabIcon: ${it.message}")
            }
        }
        Logger.logDebug("ForceDark: tab icon hooks: $hooked of ${controllers.size}")
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageDrawable",
            Drawable::class.java,
        ) { chain ->
            chain.proceed()
            if (!PrefsManager.forceDarkWebview) return@hookMethod null
            val iv = chain.thisObject as? ImageView ?: return@hookMethod null
            if (iv !in bottomTabIcons) return@hookMethod null
            Logger.logDebug("ForceDark: DrawableChangeGuard re-tinting ${iv.hashCode()}")
            iv.post { applyTabIconTint(iv) }
            null
        }
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageTintList",
            ColorStateList::class.java,
        ) { chain ->
            if (!PrefsManager.forceDarkWebview) return@hookMethod chain.proceed()
            val iv = chain.thisObject as? ImageView ?: return@hookMethod chain.proceed()
            if (iv !in bottomTabIcons) return@hookMethod chain.proceed()
            Logger.logDebug("ForceDark: TintListGuard intercepted ${iv.hashCode()}")
            chain.proceed(arrayOf(ColorStateList.valueOf(Color.rgb(168, 168, 168))))
        }
    }
}
