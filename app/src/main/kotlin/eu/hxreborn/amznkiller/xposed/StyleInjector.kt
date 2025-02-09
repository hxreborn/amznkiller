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
        val hash: Int,
    )

    private data class LastInjection(
        val url: String,
        val hash: Int,
    )

    private var cached: CachedScripts? = null
    private var validatedHash: Int = 0

    // Tracks the last successful enqueue for a given WebView to avoid repeated evaluateJavascript
    // across multiple callbacks (e.g. onPageCommitVisible + onPageFinished).
    private val lastInjectionByWebView = WeakHashMap<WebView, LastInjection>()

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

        val hash = selectors.hashCode()
        lastInjectionByWebView[webView]?.let { last ->
            if (last.url == url && last.hash == hash) return
        }

        val scripts = cached
        if (scripts == null || scripts.hash != hash) {
            // Minify rules to reduce escaping/JS payload size.
            val cssRules = selectors.joinToString(separator = "") { "$it{display:none!important;}" }
            val escaped = cssRules.replace("\\", "\\\\").replace("'", "\\'")

            val expectedRules = selectors.size
            cached =
                CachedScripts(
                    inject =
                        buildInjectScript(
                            escapedCssRules = escaped,
                            expectedRules = expectedRules,
                            hash = hash,
                            validate = false,
                        ),
                    injectAndValidate =
                        buildInjectScript(
                            escapedCssRules = escaped,
                            expectedRules = expectedRules,
                            hash = hash,
                            validate = true,
                        ),
                    hash = hash,
                )
            validatedHash = 0
            Logger.logDebug("Built CSS script: ${selectors.size} selectors")
        }

        val current = cached ?: return
        val shouldValidate = BuildConfig.DEBUG && validatedHash != hash
        if (shouldValidate) {
            validatedHash = hash
        }

        val script = if (shouldValidate) current.injectAndValidate else current.inject

        fun eval() {
            runCatching {
                webView.evaluateJavascript(script) { result ->
                    if (!shouldValidate) return@evaluateJavascript
                    if (result == null || result == "null") {
                        Logger.logDebug("CSS validation returned null")
                        return@evaluateJavascript
                    }

                    if (!result.contains("\"ok\":true")) {
                        Logger.logDebug("CSS validation failed: $result")
                    }
                }
            }.onFailure { Logger.logDebug("Failed to evaluate JS injection", it) }
        }

        // Mark before enqueue to dedupe across callbacks for the same navigation.
        lastInjectionByWebView[webView] = LastInjection(url = url, hash = hash)
        runCatching {
            if (webView.handler?.looper == Looper.getMainLooper()) {
                eval()
            } else {
                webView.post { eval() }
            }
        }.onFailure { Logger.logDebug("Failed to enqueue JS injection", it) }
    }

    private fun buildInjectScript(
        escapedCssRules: String,
        expectedRules: Int,
        hash: Int,
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

        return """
            (function() {
              var expected = $expectedRules;
              var hash = String($hash);
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
