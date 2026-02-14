package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsSnapshot
import eu.hxreborn.amznkiller.util.Logger
import java.util.concurrent.atomic.AtomicBoolean

object WebViewDebuggingGate {
    private val enabled = AtomicBoolean(false)

    fun tryEnable(prefs: PrefsSnapshot) {
        if (!prefs.webviewDebugging) return
        if (!enabled.compareAndSet(false, true)) return
        runCatching { WebView.setWebContentsDebuggingEnabled(true) }
            .onSuccess { Logger.log("WebView debugging enabled") }
            .onFailure { Logger.log("WebView debugging failed", it) }
    }
}
