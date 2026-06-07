package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.forceDarkWebview
import eu.hxreborn.amznkiller.xposed.js.ScriptId
import eu.hxreborn.amznkiller.xposed.js.ScriptRepository
import eu.hxreborn.amznkiller.xposed.js.WebViewJsExecutor
import org.json.JSONObject

object DarkModeInjector {
    fun inject(webView: WebView) {
        val enabled = forceDarkWebview
        Logger.debug { "dark inject enabled=$enabled" }
        val args = JSONObject().apply { put("enabled", enabled) }
        val script =
            ScriptRepository.get(ScriptId.DARK_MODE) + "\n" +
                "window.AmznKiller.setDarkMode($args);"
        WebViewJsExecutor.evaluate(webView, script, "DarkModeInjector")
    }
}
