package eu.hxreborn.amznkiller.selectors

import eu.hxreborn.amznkiller.util.Logger

object EmbeddedSelectors {
    fun load(): List<String> =
        runCatching {
            EmbeddedSelectors::class.java.classLoader
                ?.getResourceAsStream("payload/css/embedded.css")
                ?.bufferedReader()
                ?.use { reader ->
                    SelectorSanitizer.sanitize(reader.lineSequence())
                } ?: emptyList()
        }.getOrElse {
            Logger.log("Failed to load embedded.css", it)
            emptyList()
        }
}
