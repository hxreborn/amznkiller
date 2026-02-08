package eu.hxreborn.amznkiller.xposed

import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker
import java.util.concurrent.atomic.AtomicBoolean

object WebViewHooker {
    internal val debuggingEnabled = AtomicBoolean(false)

    fun hook() {
        val clientClass = android.webkit.WebViewClient::class.java
        for (method in clientClass.declaredMethods) {
            when (method.name) {
                "onPageFinished",
                "onPageCommitVisible",
                -> {
                    runCatching {
                        module.hook(method, PageHooker::class.java)
                    }.onSuccess { Logger.log("Hooked ${method.name}") }.onFailure {
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
            if (WebViewHooker.debuggingEnabled.compareAndSet(false, true)) {
                runCatching { WebView.setWebContentsDebuggingEnabled(true) }
                    .onSuccess {
                        Logger.log("WebView debugging enabled")
                    }.onFailure { Logger.log("WebView debugging failed", it) }
            }
            StyleInjector.inject(webView, url)
        }
    }
}
