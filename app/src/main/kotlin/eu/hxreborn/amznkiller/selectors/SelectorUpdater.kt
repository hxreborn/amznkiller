package eu.hxreborn.amznkiller.selectors

import eu.hxreborn.amznkiller.http.HttpClient

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
        return if (remote.isFailure) {
            MergeResult.Partial(selectors = all)
        } else {
            MergeResult.Success(selectors = all)
        }
    }
}
