package eu.hxreborn.amznkiller.selectors

object SelectorSanitizer {
    fun sanitize(lines: Sequence<String>): List<String> =
        lines
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("!") }
            .toList()
}
