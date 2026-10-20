package eu.hxreborn.amznkiller.xposed.injector

import android.util.Log
import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.webviewDebugging
import java.util.concurrent.atomic.AtomicBoolean

object WebViewDebuggingGate {
    private val enabled = AtomicBoolean(false)

    fun sync() {
        val wanted = webviewDebugging
        if (enabled.get() == wanted) return
        runCatching {
            WebView.setWebContentsDebuggingEnabled(wanted)
        }.onSuccess {
            enabled.set(wanted)
            Logger.debug { "webview debug enabled=$wanted" }
        }.onFailure {
            Logger.log(Log.ERROR, "webview debug fail", it)
        }
    }
}
