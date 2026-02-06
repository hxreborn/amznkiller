package eu.hxreborn.amznkiller.selectors

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.amznkiller.net.TextFetcher
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.util.Logger

object SelectorUpdater {
    // Fetch remote + merge with embedded, return deduplicated sorted set
    fun fetchMerged(url: String): Set<String> {
        val remote =
            runCatching {
                val raw = TextFetcher.fetch(url)
                SelectorSanitizer.sanitize(raw.lineSequence())
            }.getOrElse {
                Logger.log("Remote fetch failed", it)
                emptyList()
            }
        val embedded = EmbeddedSelectors.load()
        return (remote + embedded).toSortedSet()
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

        val merged = fetchMerged(url)
        if (merged.isEmpty()) {
            Logger.log("No selectors after merge, keeping existing cache")
            return
        }

        prefs.edit(commit = true) {
            Prefs.CACHED_SELECTORS.write(this, merged.joinToString("\n"))
            Prefs.LAST_FETCHED.write(this, System.currentTimeMillis())
        }
        Logger.log("Cached ${merged.size} selectors")
    }
}
