package eu.hxreborn.amznkiller.xposed.hook

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

object ForceDarkHooker {
    fun hook(xposed: XposedInterface) {
        hookActivityOnCreate(xposed)
        hookSetForceDarkAllowed(xposed)
        hookDetermineForceDarkType(xposed)
        hookRendererSetForceDark(xposed)
        hookWebViewBackground(xposed)
    }

    private fun hookActivityOnCreate(xposed: XposedInterface) {
        runCatching {
            val method =
                Activity::class.java.getDeclaredMethod(
                    "onCreate",
                    Bundle::class.java,
                )
            xposed.hook(method, ActivityOnCreateHooker::class.java)
        }.onSuccess {
            Logger.log("Hooked Activity.onCreate")
        }.onFailure {
            Logger.log("Failed to hook Activity.onCreate", it)
        }
    }

    private fun hookSetForceDarkAllowed(xposed: XposedInterface) {
        runCatching {
            val method =
                View::class.java.getDeclaredMethod(
                    "setForceDarkAllowed",
                    Boolean::class.javaPrimitiveType,
                )
            xposed.hook(method, ForceDarkOverrideHooker::class.java)
        }.onSuccess {
            Logger.log("Hooked View.setForceDarkAllowed")
        }.onFailure {
            Logger.log("Failed to hook View.setForceDarkAllowed", it)
        }
    }

    // Amazon sets android:forceDarkAllowed=false in AmazonTheme (values-v29/styles.xml)
    // ViewRootImpl.determineForceDarkType() reads this and returns 0 (NONE)
    // Override to 2 (ALWAYS) to force the GPU renderer to apply algorithmic darkening
    private fun hookDetermineForceDarkType(xposed: XposedInterface) {
        runCatching {
            val clazz = Class.forName("android.view.ViewRootImpl")
            val method = clazz.getDeclaredMethod("determineForceDarkType")
            xposed.hook(method, ForceDarkTypeHooker::class.java)
        }.onSuccess {
            Logger.log("Hooked ViewRootImpl.determineForceDarkType")
        }.onFailure {
            Logger.log("Failed to hook determineForceDarkType", it)
        }
    }

    // Fallback for older Android versions where determineForceDarkType doesn't exist
    private fun hookRendererSetForceDark(xposed: XposedInterface) {
        val classNames =
            listOf(
                "android.graphics.HardwareRenderer",
                "android.view.ThreadedRenderer",
            )
        val paramTypes =
            listOf(
                Boolean::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            )

        for (className in classNames) {
            val clazz = runCatching { Class.forName(className) }.getOrNull() ?: continue
            for (paramType in paramTypes) {
                val result =
                    runCatching {
                        val method = clazz.getDeclaredMethod("setForceDark", paramType)
                        xposed.hook(method, RendererForceDarkHooker::class.java)
                    }
                if (result.isSuccess) {
                    Logger.log("Hooked $className.setForceDark(${paramType.simpleName})")
                    return
                }
            }
        }
        Logger.log("Failed to hook setForceDark on any renderer class")
    }

    // Prevent white flash by intercepting WebView.setBackgroundColor
    // and forcing dark on all WebView constructors
    private fun hookWebViewBackground(xposed: XposedInterface) {
        for (ctor in WebView::class.java.declaredConstructors) {
            runCatching {
                xposed.hook(ctor, WebViewCtorDarkHooker::class.java)
            }.onSuccess {
                Logger.log("Hooked WebView.<init>(${ctor.parameterCount} params)")
            }.onFailure {
                Logger.log("Failed to hook WebView.<init>", it)
            }
        }

        runCatching {
            val method =
                View::class.java.getDeclaredMethod(
                    "setBackgroundColor",
                    Int::class.javaPrimitiveType,
                )
            xposed.hook(method, BackgroundColorInterceptor::class.java)
        }.onSuccess {
            Logger.log("Hooked View.setBackgroundColor")
        }.onFailure {
            Logger.log("Failed to hook View.setBackgroundColor", it)
        }
    }
}

@XposedHooker
class ActivityOnCreateHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val activity = callback.thisObject as? Activity ?: return
            runCatching {
                val decorView = activity.window?.decorView ?: return
                decorView.isForceDarkAllowed = true

                // Dark window background to prevent white flash on transitions
                activity.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
                activity.window?.statusBarColor = Color.BLACK
                activity.window?.navigationBarColor = Color.BLACK

                Logger.logDebug(
                    "ForceDark: enabled on ${activity.javaClass.simpleName}",
                )

                // Dark system bars
                activity.window?.insetsController?.let { controller ->
                    controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    )
                }
            }.onFailure {
                Logger.logDebug("ForceDark: failed on ${activity.javaClass.simpleName}", it)
            }
        }
    }
}

@XposedHooker
class ForceDarkOverrideHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val allowed = callback.args[0] as? Boolean ?: return
            if (!allowed) {
                callback.args[0] = true
                Logger.logDebug("ForceDark: overrode setForceDarkAllowed(false) -> true")
            }
        }
    }
}

@XposedHooker
class ForceDarkTypeHooker : XposedInterface.Hooker {
    companion object {
        private const val FORCE_DARK_NONE = 0
        private const val FORCE_DARK_ALWAYS = 2

        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            val result = callback.result as? Int ?: return
            if (result == FORCE_DARK_NONE) {
                callback.result = FORCE_DARK_ALWAYS
                Logger.logDebug(
                    "ForceDark: overrode determineForceDarkType $result -> $FORCE_DARK_ALWAYS",
                )
            }
        }
    }
}

@XposedHooker
class RendererForceDarkHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: BeforeHookCallback) {
            if (!PrefsManager.forceDarkWebview) return
            when (val arg = callback.args[0]) {
                is Boolean -> {
                    if (!arg) {
                        callback.args[0] = true
                        Logger.logDebug("ForceDark: overrode setForceDark(false) -> true")
                    }
                }

                is Int -> {
                    if (arg != 2) {
                        callback.args[0] = 2
                        Logger.logDebug("ForceDark: overrode setForceDark($arg) -> 2")
                    }
                }
            }
        }
    }
}

// Force dark background right after WebView is constructed
// Using Color.TRANSPARENT so the dark Activity window background shows through
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

// Intercept any View.setBackgroundColor call on a WebView
// Prevent Amazon from resetting it to white
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
