package eu.hxreborn.amznkiller.xposed.injector

import android.util.Log
import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.webviewDebugging
import java.util.concurrent.atomic.AtomicBoolean

object WebViewDebuggingGate {
    private val enabled = AtomicBoolean(false)

    fun tryEnable() {
        if (!webviewDebugging) return
        if (!enabled.compareAndSet(false, true)) return
        runCatching {
            WebView.setWebContentsDebuggingEnabled(true)
        }.onSuccess {
            Logger.debug { "webview debug enabled" }
        }.onFailure {
            Logger.log(Log.ERROR, "webview debug fail", it)
        }
    }
}
