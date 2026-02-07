package eu.hxreborn.amznkiller.selectors

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.amznkiller.http.HttpClient
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.util.Logger

sealed class MergeResult {
    abstract val selectors: Set<String>

    data class Success(
        override val selectors: Set<String>,
    ) : MergeResult()

    data class Partial(
        override val selectors: Set<String>,
        val error: Throwable,
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
            Logger.log("Remote fetch failed", error)
            return MergeResult.Partial(selectors = all, error = error)
        }
        return MergeResult.Success(selectors = all)
    }

    // Fetch, merge, and write to SharedPreferences
    fun refresh(prefs: SharedPreferences) {
        val url = Prefs.SELECTOR_URL.read(prefs)
        if (url.isBlank()) {
            Logger.log("No selector URL configured, using embedded only")
            val embedded = EmbeddedSelectors.load()
            if (embedded.isEmpty()) return
            prefs.edit(commit = true) {
                Prefs.CACHED_SELECTORS.write(this, embedded.sorted().joinToString("\n"))
                Prefs.LAST_FETCHED.write(this, System.currentTimeMillis())
            }
            Logger.log("Cached ${embedded.size} embedded selectors")
            return
        }

        val result = fetchMerged(url)
        if (result.selectors.isEmpty()) {
            Logger.log("No selectors after merge, keeping existing cache")
            return
        }

        prefs.edit(commit = true) {
            Prefs.CACHED_SELECTORS.write(this, result.selectors.joinToString("\n"))
            Prefs.LAST_FETCHED.write(this, System.currentTimeMillis())
        }
        Logger.log("Cached ${result.selectors.size} selectors")
    }
}
