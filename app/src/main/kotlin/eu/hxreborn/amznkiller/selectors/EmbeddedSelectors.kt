package eu.hxreborn.amznkiller.selectors

import eu.hxreborn.amznkiller.util.Logger

object EmbeddedSelectors {
    fun load(): List<String> =
        runCatching {
            EmbeddedSelectors::class.java.classLoader
                ?.getResourceAsStream("payload/embedded.css")
                ?.bufferedReader()
                ?.lineSequence()
                ?.let { SelectorSanitizer.sanitize(it) }
                ?: emptyList()
        }.getOrElse {
            Logger.log("Failed to load embedded.css", it)
            emptyList()
        }
}
