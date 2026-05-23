package eu.hxreborn.amznkiller.xposed.js

import android.os.Looper
import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger

object WebViewJsExecutor {
    fun evaluate(
        webView: WebView,
        script: String,
        tag: String,
        onResult: ((String?) -> Unit)? = null,
    ) {
        fun run() {
            runCatching {
                webView.evaluateJavascript(script) { onResult?.invoke(it) }
            }.onFailure { Logger.debug { "$tag: eval failed: ${it.message}" } }
        }

        runCatching {
            if (webView.handler?.looper == Looper.getMainLooper()) {
                run()
            } else {
                webView.post { run() }
            }
        }.onFailure { Logger.debug { "$tag: post failed: ${it.message}" } }
    }
}
