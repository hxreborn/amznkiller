package eu.hxreborn.amznkiller.xposed.injector

import android.webkit.WebView
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.cachedInjectionEnabled
import eu.hxreborn.amznkiller.xposed.hook.cachedSelectors
import eu.hxreborn.amznkiller.xposed.js.ScriptId
import eu.hxreborn.amznkiller.xposed.js.ScriptRepository
import eu.hxreborn.amznkiller.xposed.js.WebViewJsExecutor
import org.json.JSONObject
import java.util.WeakHashMap

object CssInjector {
    private data class InjectionKey(
        val url: String,
        val selectorsHash: Int,
    )

    private val lastInjectionByWebView = WeakHashMap<WebView, InjectionKey>()
    private var cachedCss: String? = null
    private var cachedHash: Int = 0
    private var lastValidatedHash: Int = 0

    fun inject(
        webView: WebView,
        url: String,
    ) {
        if (!cachedInjectionEnabled) return
        val selectors = cachedSelectors
        if (selectors.isEmpty()) {
            Logger.debug { "css skip reason=empty-selectors" }
            return
        }

        val hash = selectors.hashCode()
        lastInjectionByWebView[webView]?.let { last ->
            if (last.url == url && last.selectorsHash == hash) return
        }

        val css = getOrBuildCss(selectors, hash)
        val shouldValidate = BuildConfig.DEBUG && lastValidatedHash != hash
        if (shouldValidate) lastValidatedHash = hash

        val args =
            JSONObject().apply {
                put("css", css)
                put("hash", hash)
                put("validate", shouldValidate)
                put("expectedRules", selectors.size)
            }
        val script = "${
            ScriptRepository.get(
                ScriptId.AD_BLOCK,
            )
        }\nwindow.AmznKiller.blockAds($args);"
        lastInjectionByWebView[webView] = InjectionKey(url, hash)

        WebViewJsExecutor.evaluate(webView, script, "CssInjector") { result ->
            if (result == null || result == "null" || result.contains("\"ok\":true")) {
                return@evaluate
            }
            Logger.debug { "css validate result=$result" }
        }
    }

    private fun getOrBuildCss(
        selectors: List<String>,
        hash: Int,
    ): String {
        if (cachedHash == hash) cachedCss?.let { return it }
        val hideRules = selectors.joinToString("") { "$it{display:none!important;}" }
        val whitelist =
            "#amznkiller-charts,#amznkiller-charts *" +
                "{display:block!important;visibility:visible!important;" +
                "opacity:1!important;}"
        return (hideRules + whitelist).also {
            cachedCss = it
            cachedHash = hash
        }
    }
}
