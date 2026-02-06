package eu.hxreborn.amznkiller.prefs

object Prefs {
    const val GROUP = "amznkiller"
    const val STALE_THRESHOLD_MS = 24 * 60 * 60 * 1000L

    val SELECTOR_URL =
        StringPref(
            "selector_url",
            "https://raw.githubusercontent.com/hxreborn/amznkiller/main/app/src/main/resources/payload/selectors-remote.css",
        )
    val CACHED_SELECTORS = StringPref("cached_selectors", "")
    val LAST_FETCHED = LongPref("last_fetched", 0L)
    val DEBUG_LOGS = BoolPref("debug_logs", false)

    val DARK_THEME_CONFIG = StringPref("dark_theme_config", "follow_system")
    val USE_DYNAMIC_COLOR = BoolPref("use_dynamic_color", true)

    val all: List<PrefSpec<*>> =
        listOf(
            SELECTOR_URL,
            CACHED_SELECTORS,
            LAST_FETCHED,
            DEBUG_LOGS,
            DARK_THEME_CONFIG,
            USE_DYNAMIC_COLOR,
        )
}
