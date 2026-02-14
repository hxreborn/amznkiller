package eu.hxreborn.amznkiller.xposed.hook

import android.webkit.WebView
import android.webkit.WebViewClient
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.runtime.PageRuntime
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker

object WebViewHooker {
    fun hook(xposed: XposedInterface) {
        for (method in WebViewClient::class.java.declaredMethods) {
            when (method.name) {
                "onPageStarted" -> {
                    runCatching {
                        xposed.hook(method, PageStartedHooker::class.java)
                    }.onSuccess { Logger.log("Hooked ${method.name}") }
                        .onFailure {
                            Logger.log("Failed to hook ${method.name}", it)
                        }
                }

                "onPageFinished",
                "onPageCommitVisible",
                -> {
                    runCatching { xposed.hook(method, PageHooker::class.java) }
                        .onSuccess { Logger.log("Hooked ${method.name}") }
                        .onFailure {
                            Logger.log("Failed to hook ${method.name}", it)
                        }
                }
            }
        }
    }
}

@XposedHooker
class PageStartedHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            val webView = callback.args[0] as? WebView ?: return
            PageRuntime.onPageStarted(webView)
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
            PageRuntime.onPageLoaded(webView, url)
        }
    }
}
