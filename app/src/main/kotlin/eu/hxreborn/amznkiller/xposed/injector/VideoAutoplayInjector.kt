package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.disableVideoAutoplay
import eu.hxreborn.amznkiller.xposed.js.ScriptId
import eu.hxreborn.amznkiller.xposed.js.ScriptRepository
import eu.hxreborn.amznkiller.xposed.js.WebViewJsExecutor
import org.json.JSONObject

object VideoAutoplayInjector {
    fun inject(webView: WebView) {
        if (!disableVideoAutoplay) return
        Logger.debug { "autoplay inject" }
        val args = JSONObject().apply { put("enabled", true) }
        val script =
            ScriptRepository.get(ScriptId.VIDEO_AUTOPLAY) + "\n" +
                "window.AmznKiller.disableVideoAutoplay($args);"
        WebViewJsExecutor.evaluate(webView, script, "VideoAutoplayInjector")
    }
}
