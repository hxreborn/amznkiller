package eu.hxreborn.amznkiller.xposed

import android.webkit.WebView
import android.webkit.WebViewClient
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker
import java.util.concurrent.atomic.AtomicBoolean

object WebViewHooker {
    internal val debuggingEnabled = AtomicBoolean(false)

    fun hook() {
        val clientClass = WebViewClient::class.java
        for (method in clientClass.declaredMethods) {
            when (method.name) {
                "onPageFinished",
                "onPageCommitVisible",
                -> {
                    val hookResult =
                        runCatching {
                            module.hook(method, PageHooker::class.java)
                        }
                    hookResult.onSuccess {
                        Logger.log("Hooked ${method.name}")
                    }
                    hookResult.onFailure {
                        Logger.log("Failed to hook ${method.name}", it)
                    }
                }
            }
        }
    }
}

@XposedHooker
class PageHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            val webView = callback.args[0] as? WebView ?: return
            val url = callback.args[1] as? String ?: return
            if (!url.contains("amazon.")) return
            if (PrefsManager.webviewDebugging &&
                WebViewHooker.debuggingEnabled.compareAndSet(
                    false,
                    true,
                )
            ) {
                val debuggingResult =
                    runCatching {
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                debuggingResult.onSuccess {
                    Logger.log("WebView debugging enabled")
                }
                debuggingResult.onFailure {
                    Logger.log("WebView debugging failed", it)
                }
            }
            StyleInjector.inject(webView, url)
        }
    }
}
