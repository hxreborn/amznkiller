package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsSnapshot
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.js.ScriptId
import eu.hxreborn.amznkiller.xposed.js.ScriptRepository
import eu.hxreborn.amznkiller.xposed.js.WebViewJsExecutor
import org.json.JSONObject

object DarkModeInjector {
    fun inject(
        webView: WebView,
        prefs: PrefsSnapshot,
    ) {
        Logger.logDebug("DarkModeInjector: enabled=${prefs.forceDarkWebview}")
        val args = JSONObject().apply { put("enabled", prefs.forceDarkWebview) }
        val script =
            ScriptRepository.get(ScriptId.DARK_MODE) + "\n" +
                "window.AmznKiller.setDarkMode($args);"
        WebViewJsExecutor.evaluate(webView, script, "DarkModeInjector")
    }
}
