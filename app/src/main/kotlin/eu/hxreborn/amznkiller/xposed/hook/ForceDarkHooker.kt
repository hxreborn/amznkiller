package eu.hxreborn.amznkiller.xposed.hook

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.ImageView
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import java.util.Collections
import java.util.WeakHashMap

object ForceDarkHooker {
    val bottomTabIcons: MutableSet<View> =
        Collections.newSetFromMap(WeakHashMap())

    // GPU force dark inverts this grey to near-white
    private val TAB_ICON_TINT = Color.rgb(168, 168, 168)
    private val TAB_ICON_CSL = ColorStateList.valueOf(TAB_ICON_TINT)

    lateinit var hostClassLoader: ClassLoader

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        hostClassLoader = classLoader
        hookMethod(xposed, Activity::class.java, "onCreate", Bundle::class.java) {
            ActivityOnCreateHooker::class.java
        }
        hookDetermineForceDarkType(xposed)
        hookRendererSetForceDark(xposed)
        hookWebViewBackground(xposed)
        hookTabIcons(xposed)
    }

    fun applyTabIconTint(imageView: ImageView) {
        bottomTabIcons.add(imageView)
        imageView.imageTintList = TAB_ICON_CSL
        imageView.colorFilter =
            PorterDuffColorFilter(TAB_ICON_TINT, PorterDuff.Mode.SRC_IN)
    }

    private fun hookMethod(
        xposed: XposedInterface,
        clazz: Class<*>,
        name: String,
        vararg params: Class<*>,
        hooker: () -> Class<out XposedInterface.Hooker>,
    ) {
        runCatching {
            xposed.hook(clazz.getDeclaredMethod(name, *params), hooker())
        }.onSuccess {
            Logger.log("Hooked ${clazz.simpleName}.$name")
        }.onFailure {
            Logger.log("Failed to hook ${clazz.simpleName}.$name", it)
        }
    }

    // Amazon sets android:forceDarkAllowed=false in AmazonTheme
    // Override determineForceDarkType result from 0 (NONE) to 2 (ALWAYS)
    private fun hookDetermineForceDarkType(xposed: XposedInterface) {
        runCatching {
            val clazz = Class.forName("android.view.ViewRootImpl")
            xposed.hook(
                clazz.getDeclaredMethod("determineForceDarkType"),
                ForceDarkTypeHooker::class.java,
            )
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
            val clazz =
                runCatching { Class.forName(cls) }.getOrNull() ?: continue
            for (param in params) {
                val ok =
                    runCatching {
                        xposed.hook(
                            clazz.getDeclaredMethod("setForceDark", param),
                            RendererForceDarkHooker::class.java,
                        )
                    }
                if (ok.isSuccess) {
                    Logger.log(
                        "Hooked $cls.setForceDark(${param.simpleName})",
                    )
                    return
                }
            }
        }
        Logger.log("Failed to hook setForceDark on any renderer class")
    }

    private fun hookWebViewBackground(xposed: XposedInterface) {
        for (ctor in WebView::class.java.declaredConstructors) {
            runCatching {
                xposed.hook(ctor, WebViewCtorDarkHooker::class.java)
            }
        }
        hookMethod(
            xposed,
            View::class.java,
            "setBackgroundColor",
            Int::class.javaPrimitiveType!!,
        ) { BackgroundColorInterceptor::class.java }
    }

    private fun hookTabIcons(xposed: XposedInterface) {
        val controllers =
            listOf(
                "com.amazon.mShop.chrome.bottomtabs.BaseTabController",
                "com.amazon.mShop.chrome.bottomtabs.SavXTabController",
                "com.amazon.mShop.chrome.bottomtabs.SwitcherTabController",
            )
        for (cls in controllers) {
            runCatching {
                val clazz =
                    Class.forName(cls, false, hostClassLoader)
                xposed.hook(
                    clazz.declaredMethods.first { it.name == "getTabIcon" },
                    GetTabIconHooker::class.java,
                )
            }.onSuccess {
                Logger.log("Hooked $cls.getTabIcon")
            }.onFailure {
                Logger.logDebug("Failed to hook $cls.getTabIcon", it)
            }
        }
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageDrawable",
            android.graphics.drawable.Drawable::class.java,
        ) { DrawableChangeGuard::class.java }
        hookMethod(
            xposed,
            ImageView::class.java,
            "setImageTintList",
            ColorStateList::class.java,
        ) { TintListGuard::class.java }
    }
}

// Enable force dark on activity window and darken system bars
@XposedHooker
class ActivityOnCreateHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val activity = callback.thisObject as? Activity ?: return
            runCatching {
                activity.window?.decorView?.let { it.isForceDarkAllowed = true }
                activity.window?.setBackgroundDrawable(
                    ColorDrawable(Color.BLACK),
                )
                activity.window?.statusBarColor = Color.BLACK
                activity.window?.navigationBarColor = Color.BLACK
                activity.window?.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                )
            }
        }
    }
}

// Override ViewRootImpl.determineForceDarkType from NONE(0) to ALWAYS(2)
@XposedHooker
class ForceDarkTypeHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val result = callback.result as? Int ?: return
            if (result == 0) callback.result = 2
        }
    }
}

// Fallback for older Android: force dark on HardwareRenderer/ThreadedRenderer
@XposedHooker
class RendererForceDarkHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            when (val arg = callback.args[0]) {
                is Boolean -> if (!arg) callback.args[0] = true
                is Int -> if (arg != 2) callback.args[0] = 2
            }
        }
    }
}

// Make WebView background transparent so the dark activity window shows through
@XposedHooker
class WebViewCtorDarkHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val webView = callback.thisObject as? WebView ?: return
            runCatching {
                webView.setBackgroundColor(Color.TRANSPARENT)
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
        }
    }
}

// Block Amazon from resetting WebView backgrounds to white
@XposedHooker
class BackgroundColorInterceptor : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            if (callback.thisObject !is WebView) return
            val color = callback.args[0] as? Int ?: return
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (r > 200 && g > 200 && b > 200) {
                callback.args[0] = Color.TRANSPARENT
            }
        }
    }
}

// Tint bottom tab icons grey so GPU force dark inverts them to near-white
@XposedHooker
class GetTabIconHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val icon = callback.result as? ImageView ?: return
            ForceDarkHooker.applyTabIconTint(icon)
        }
    }
}

// Prevent Amazon tab controllers from overriding our icon tint
@XposedHooker
class TintListGuard : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val iv = callback.thisObject as? ImageView ?: return
            if (iv !in ForceDarkHooker.bottomTabIcons) return
            callback.args[0] =
                ColorStateList.valueOf(Color.rgb(168, 168, 168))
        }
    }
}

// Re-apply grey tint after a tab icon drawable swap (animations, tab switch)
@XposedHooker
class DrawableChangeGuard : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val iv = callback.thisObject as? ImageView ?: return
            if (iv !in ForceDarkHooker.bottomTabIcons) return
            iv.post { ForceDarkHooker.applyTabIconTint(iv) }
        }
    }
}
