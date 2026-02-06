package eu.hxreborn.amznkiller.selectors

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.amznkiller.net.TextFetcher
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.util.Logger

data class MergeResult(
    val selectors: Set<String>,
    val remoteFailed: Boolean,
)

object SelectorUpdater {
    fun fetchMerged(url: String): MergeResult {
        val remote =
            runCatching {
                val raw = TextFetcher.fetch(url)
                SelectorSanitizer.sanitize(raw.lineSequence())
            }
        if (remote.isFailure) {
            Logger.log("Remote fetch failed", remote.exceptionOrNull())
        }
        val embedded = EmbeddedSelectors.load()
        val all = (remote.getOrDefault(emptyList()) + embedded).toSortedSet()
        return MergeResult(selectors = all, remoteFailed = remote.isFailure)
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
