package eu.hxreborn.amznkiller.xposed.hook

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.runtime.PageRuntime
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Method

object WebViewHook {
    fun hook(xposed: XposedInterface) {
        for (method in WebViewClient::class.java.declaredMethods) {
            when (method.name) {
                "onPageStarted" -> {
                    hookGuarded(method) {
                        xposed.hook(method).intercept { chain ->
                            chain.proceed()
                            val webView = chain.getArg(0) as? WebView ?: return@intercept null
                            PageRuntime.onPageStarted(webView)
                            null
                        }
                    }
                }

                "onPageFinished",
                "onPageCommitVisible",
                -> {
                    hookGuarded(method) {
                        xposed.hook(method).intercept { chain ->
                            chain.proceed()
                            val webView = chain.getArg(0) as? WebView ?: return@intercept null
                            val url = chain.getArg(1) as? String ?: return@intercept null
                            PageRuntime.onPageLoaded(webView, url)
                            null
                        }
                    }
                }
            }
        }
    }

    private inline fun hookGuarded(
        method: Method,
        install: () -> Unit,
    ) {
        try {
            install()
            Logger.debug { "hooked method=${method.name}" }
        } catch (e: Exception) {
            Logger.log(Log.ERROR, "hook fail method=${method.name}", e)
        } catch (e: LinkageError) {
            Logger.log(Log.ERROR, "hook fail method=${method.name}", e)
        }
    }
}
