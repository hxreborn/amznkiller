package eu.hxreborn.amznkiller.xposed

import android.webkit.WebView
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger

object StyleInjector {
    private data class CachedScripts(
        val inject: String,
        val injectAndValidate: String,
        val hash: Int,
    )

    private var cached: CachedScripts? = null
    private var validatedHash: Int = 0

    fun inject(webView: WebView) {
        if (!PrefsManager.injectionEnabled) return
        val selectors = PrefsManager.selectors
        if (selectors.isEmpty()) {
            Logger.log("inject: no selectors")
            return
        }

        val hash = selectors.hashCode()
        val scripts = cached
        if (scripts == null || scripts.hash != hash) {
            val cssRules =
                selectors.joinToString("\n") { "$it { display: none !important; }" }
            val escaped =
                cssRules
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")

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
            Logger.log("Built CSS script: ${selectors.size} selectors")
        }

        val current = cached ?: return
        val shouldValidate = BuildConfig.DEBUG && validatedHash != hash
        if (shouldValidate) {
            validatedHash = hash
        }

        val script = if (shouldValidate) current.injectAndValidate else current.inject

        runCatching {
            webView.post {
                runCatching {
                    webView.evaluateJavascript(script) { result ->
                        if (!shouldValidate) return@evaluateJavascript
                        if (result == null || result == "null") {
                            Logger.log("CSS validation returned null")
                            return@evaluateJavascript
                        }

                        if (!result.contains("\"ok\":true")) {
                            Logger.log("CSS validation failed: $result")
                        }
                    }
                }.onFailure { Logger.log("Failed to evaluate JS injection", it) }
            }
        }.onFailure { Logger.log("Failed to post JS injection", it) }
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
