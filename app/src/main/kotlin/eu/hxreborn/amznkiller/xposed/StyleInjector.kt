package eu.hxreborn.amznkiller.xposed

import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger

object StyleInjector {
    @Volatile
    private var cachedScript: String? = null

    @Volatile
    private var cachedHash: Int = 0

    fun inject(webView: WebView) {
        val selectors = PrefsManager.selectors
        if (selectors.isEmpty()) {
            Logger.log("inject: no selectors")
            return
        }

        val hash = selectors.hashCode()
        if (hash != cachedHash || cachedScript == null) {
            val cssRules =
                selectors.joinToString("\n") { "$it { display: none !important; }" }
            val escaped =
                cssRules
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
            cachedScript =
                """
                (function() {
                  if (document.getElementById('amznkiller')) return;
                  var s = document.createElement('style');
                  s.id = 'amznkiller';
                  s.textContent = '$escaped';
                  (document.head || document.documentElement).appendChild(s);
                })();
                """.trimIndent()
            cachedHash = hash
            Logger.log("Built CSS script: ${selectors.size} selectors")
        }

        cachedScript?.let { script ->
            webView.evaluateJavascript(script, null)
        }
    }
}
