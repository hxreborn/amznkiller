package eu.hxreborn.amznkiller.prefs

object Prefs {
    const val GROUP = "amznkiller"
    const val STALE_THRESHOLD_MS = 24 * 60 * 60 * 1000L

    // TODO: //check if using duration feels ok here
    val SELECTOR_URL =
        StringPref(
            "selector_url",
            "https://raw.githubusercontent.com/hxreborn/amznkiller/main/lists/generated/merged.txt",
        )
    val CACHED_SELECTORS = StringPref("cached_selectors", "")
    val LAST_FETCHED = LongPref("last_fetched", 0L)
    val DEBUG_LOGS = BoolPref("debug_logs", false)
    val INJECTION_ENABLED = BoolPref("injection_enabled", true)
    val WEBVIEW_DEBUGGING = BoolPref("webview_debugging", false)

    val AUTO_UPDATE = BoolPref("auto_update", true)

    val DARK_THEME_CONFIG = StringPref("dark_theme_config", "follow_system")
    val USE_DYNAMIC_COLOR = BoolPref("use_dynamic_color", true)

    val all: List<PrefSpec<*>> =
        listOf(
            SELECTOR_URL,
            CACHED_SELECTORS,
            LAST_FETCHED,
            DEBUG_LOGS,
            INJECTION_ENABLED,
            WEBVIEW_DEBUGGING,
            AUTO_UPDATE,
            DARK_THEME_CONFIG,
            USE_DYNAMIC_COLOR,
        )

    fun parseSelectors(raw: String): List<String> = raw.lines().filter { it.isNotBlank() }
}
