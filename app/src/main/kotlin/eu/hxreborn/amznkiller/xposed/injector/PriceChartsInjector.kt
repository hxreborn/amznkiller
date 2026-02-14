package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsSnapshot
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.js.ScriptId
import eu.hxreborn.amznkiller.xposed.js.ScriptRepository
import eu.hxreborn.amznkiller.xposed.js.WebViewJsExecutor
import eu.hxreborn.amznkiller.xposed.runtime.AmazonUrlInfo
import org.json.JSONObject

object PriceChartsInjector {
    private val KEEPA_DOMAINS =
        mapOf(
            "amazon.com" to 1,
            "amazon.co.uk" to 2,
            "amazon.de" to 3,
            "amazon.fr" to 4,
            "amazon.co.jp" to 5,
            "amazon.ca" to 6,
            "amazon.it" to 8,
            "amazon.es" to 9,
            "amazon.in" to 10,
            "amazon.com.mx" to 11,
            "amazon.com.br" to 12,
            "amazon.com.au" to 13,
        )

    private val CAMEL_LOCALES =
        mapOf(
            "amazon.com" to "us",
            "amazon.co.uk" to "uk",
            "amazon.de" to "de",
            "amazon.fr" to "fr",
            "amazon.co.jp" to "jp",
            "amazon.ca" to "ca",
            "amazon.it" to "it",
            "amazon.es" to "es",
            "amazon.com.au" to "au",
        )

    fun inject(
        webView: WebView,
        prefs: PrefsSnapshot,
        amazon: AmazonUrlInfo,
    ) {
        if (!prefs.priceChartsEnabled) return
        if (!amazon.isProductPage) return
        val asin = amazon.asin ?: return
        val domain = amazon.domain ?: return
        val keepaId = KEEPA_DOMAINS[domain] ?: 1
        val camelLocale = CAMEL_LOCALES[domain] ?: "us"

        Logger.logDebug("PriceChartsInjector: $asin on $domain")
        val args =
            JSONObject().apply {
                put("asin", asin)
                put("domain", domain)
                put("keepaId", keepaId)
                put("camelLocale", camelLocale)
                put("dark", prefs.forceDarkWebview)
            }
        val script =
            ScriptRepository.get(ScriptId.CHARTS) + "\n" + "window.AmznKiller.injectCharts($args);"
        WebViewJsExecutor.evaluate(webView, script, "PriceChartsInjector") {
            Logger.logDebug("PriceChartsInjector result: $it")
        }
    }
}
