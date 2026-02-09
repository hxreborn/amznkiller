package eu.hxreborn.amznkiller.xposed

import android.os.Looper
import android.webkit.WebView
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import java.util.WeakHashMap

object StyleInjector {
    private data class CachedScripts(
        val inject: String,
        val injectAndValidate: String,
        val selectorsHash: Int,
    )

    private data class InjectionKey(
        val url: String,
        val selectorsHash: Int,
    )

    private var cached: CachedScripts? = null
    private var lastValidatedHash: Int = 0

    // Tracks last injection per WebView to skip duplicate callbacks (onPageCommitVisible + onPageFinished)
    private val lastInjectionByWebView = WeakHashMap<WebView, InjectionKey>()

    fun inject(
        webView: WebView,
        url: String,
    ) {
        if (!PrefsManager.injectionEnabled) return
        val selectors = PrefsManager.selectors
        if (selectors.isEmpty()) {
            Logger.logDebug("inject: no selectors")
            return
        }

        val selectorsHash = selectors.hashCode()
        lastInjectionByWebView[webView]?.let { last ->
            if (last.url == url && last.selectorsHash == selectorsHash) return
        }

        val script = getOrBuildScript(selectors, selectorsHash)
        lastInjectionByWebView[webView] = InjectionKey(url = url, selectorsHash = selectorsHash)
        evaluateScript(webView, script)
    }

    // Caches bare inject and inject-with-validation scripts, validation runs once per hash change in debug only
    private fun getOrBuildScript(
        selectors: List<String>,
        selectorsHash: Int,
    ): String {
        val scripts = cached
        if (scripts == null || scripts.selectorsHash != selectorsHash) {
            val cssRules = selectors.joinToString(separator = "") { "$it{display:none!important;}" }
            val escaped = cssRules.replace("\\", "\\\\").replace("'", "\\'")
            val expectedRules = selectors.size
            cached =
                CachedScripts(
                    inject =
                        buildStyleScript(
                            escapedCssRules = escaped,
                            expectedRules = expectedRules,
                            selectorsHash = selectorsHash,
                            validate = false,
                        ),
                    injectAndValidate =
                        buildStyleScript(
                            escapedCssRules = escaped,
                            expectedRules = expectedRules,
                            selectorsHash = selectorsHash,
                            validate = true,
                        ),
                    selectorsHash = selectorsHash,
                )
            lastValidatedHash = 0
            Logger.logDebug("Built CSS script: ${selectors.size} selectors")
        }

        val current = cached!!
        val shouldValidate = BuildConfig.DEBUG && lastValidatedHash != selectorsHash
        if (shouldValidate) lastValidatedHash = selectorsHash
        return if (shouldValidate) current.injectAndValidate else current.inject
    }

    // evaluateJavascript needs the WebView's looper, post defensively since Xposed can alter dispatch
    private fun evaluateScript(
        webView: WebView,
        script: String,
    ) {
        fun run() {
            runCatching {
                webView.evaluateJavascript(script) { result ->
                    if (result == null || result == "null" || result.contains("\"ok\":true")) {
                        return@evaluateJavascript
                    }
                    Logger.logDebug("CSS validation failed: $result")
                }
            }.onFailure { Logger.logDebug("Failed to evaluate JS injection", it) }
        }

        runCatching {
            if (webView.handler?.looper == Looper.getMainLooper()) {
                run()
            } else {
                webView.post { run() }
            }
        }.onFailure { Logger.logDebug("Failed to post JS injection", it) }
    }

    private fun buildStyleScript(
        escapedCssRules: String,
        expectedRules: Int,
        selectorsHash: Int,
        validate: Boolean,
    ): String {
        val validateJs =
            if (!validate) {
                ""
            } else {
                """
                try {
                  var parsed = 0;
                  if (s.sheet && s.sheet.cssRules) parsed = s.sheet.cssRules.length;
                  return {
                    ok: parsed === expected,
                    expected: expected,
                    parsed: parsed,
                    delta: expected - parsed
                  };
                } catch (e) {
                  return { ok: false, expected: expected, error: String(e) };
                }
                """.trimIndent()
            }

        // DOM-side hash check so re-hooking the same page (eg SPA navigation) skips textContent replacement when selectors haven't changed
        return """
            (function() {
              var expected = $expectedRules;
              var hash = String($selectorsHash);
              var s = document.getElementById('amznkiller');
              if (!s) {
                s = document.createElement('style');
                s.id = 'amznkiller';
                (document.head || document.documentElement).appendChild(s);
              }
              var prev = s.getAttribute('data-amznkiller-hash');
              if (prev !== hash) {
                s.setAttribute('data-amznkiller-hash', hash);
                s.textContent = '$escapedCssRules';
              }
              $validateJs
            })();
            """.trimIndent()
    }
}
