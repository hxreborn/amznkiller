package eu.hxreborn.amznkiller.xposed.hook

import android.webkit.WebView
import android.webkit.WebViewClient
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.runtime.PageRuntime
import io.github.libxposed.api.XposedInterface

object WebViewHooker {
    fun hook(xposed: XposedInterface) {
        for (method in WebViewClient::class.java.declaredMethods) {
            when (method.name) {
                "onPageStarted" -> {
                    runCatching {
                        xposed.hook(method).intercept { chain ->
                            chain.proceed()
                            val webView =
                                chain.getArg(0) as? WebView ?: return@intercept null
                            PageRuntime.onPageStarted(webView)
                            null
                        }
                    }.onSuccess {
                        Logger.log("Hooked ${method.name}")
                    }.onFailure {
                        Logger.log("Failed to hook ${method.name}", it)
                    }
                }

                "onPageFinished",
                "onPageCommitVisible",
                -> {
                    runCatching {
                        xposed.hook(method).intercept { chain ->
                            chain.proceed()
                            val webView =
                                chain.getArg(0) as? WebView ?: return@intercept null
                            val url =
                                chain.getArg(1) as? String ?: return@intercept null
                            PageRuntime.onPageLoaded(webView, url)
                            null
                        }
                    }.onSuccess {
                        Logger.log("Hooked ${method.name}")
                    }.onFailure {
                        Logger.log("Failed to hook ${method.name}", it)
                    }
                }
            }
        }
    }
}
