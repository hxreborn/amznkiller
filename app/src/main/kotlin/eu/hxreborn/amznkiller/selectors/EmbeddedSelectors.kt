package eu.hxreborn.amznkiller.selectors

import android.util.Log

object EmbeddedSelectors {
    private const val TAG = "AmznKiller/Embedded"

    fun load(): List<String> =
        runCatching {
            EmbeddedSelectors::class.java.classLoader
                ?.getResourceAsStream("payload/css/embedded.css")
                ?.bufferedReader()
                ?.use { reader ->
                    SelectorSanitizer.sanitize(reader.lineSequence())
                } ?: emptyList()
        }.getOrElse {
            Log.e(TAG, "embedded load fail file=embedded.css", it)
            emptyList()
        }
}
