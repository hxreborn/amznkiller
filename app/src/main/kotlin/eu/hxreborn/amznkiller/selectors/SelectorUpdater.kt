package eu.hxreborn.amznkiller.selectors

import android.content.SharedPreferences
import eu.hxreborn.amznkiller.http.HttpClient
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger

sealed class MergeResult {
    abstract val selectors: Set<String>

    data class Success(
        override val selectors: Set<String>,
    ) : MergeResult()

    data class Partial(
        override val selectors: Set<String>,
    ) : MergeResult()
}

object SelectorUpdater {
    fun fetchMerged(url: String): MergeResult {
        val remote =
            runCatching {
                val raw = HttpClient.fetch(url)
                SelectorSanitizer.sanitize(raw.lineSequence())
            }
        val embedded = EmbeddedSelectors.load()
        val all = (remote.getOrDefault(emptyList()) + embedded).toSortedSet()
        val error = remote.exceptionOrNull()
        if (error != null) {
            Logger.debug { "Remote fetch failed: ${error.message}" }
            return MergeResult.Partial(selectors = all)
        }
        return MergeResult.Success(selectors = all)
    }

    fun refresh(prefs: SharedPreferences) {
        val url = Prefs.SELECTOR_URL.read(prefs)
        if (url.isBlank()) {
            Logger.debug { "No selector URL configured, using embedded only" }
            val embedded = EmbeddedSelectors.load()
            if (embedded.isEmpty()) return
            PrefsManager.setFallbackSelectors(embedded.sorted())
            Logger.debug { "Cached ${embedded.size} embedded selectors" }
            return
        }

        val result = fetchMerged(url)
        if (result.selectors.isEmpty()) {
            Logger.debug { "No selectors after merge, keeping existing cache" }
            return
        }

        PrefsManager.setFallbackSelectors(result.selectors.toList())
        Logger.debug { "Cached ${result.selectors.size} selectors" }
    }
}
