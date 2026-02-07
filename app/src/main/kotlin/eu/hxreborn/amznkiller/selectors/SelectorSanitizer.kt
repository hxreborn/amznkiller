package eu.hxreborn.amznkiller.selectors

object SelectorSanitizer {
    fun sanitize(lines: Sequence<String>): List<String> =
        lines
            .map { it.trim() }
            .filter(::isSafe)
            .toList()

    private fun isSafe(s: String): Boolean {
        if (s.isEmpty()) return false
        if (s.startsWith("!")) return false
        if ("##" in s || "#@#" in s) return false
        if ("{" in s || "}" in s) return false
        if ("/*" in s || "*/" in s) return false
        if ("\u0000" in s) return false
        if ("\r" in s || "\n" in s) return false
        if (s.startsWith(">") || s.startsWith("+") || s.startsWith("~")) return false
        return true
    }
}
