package eu.hxreborn.amznkiller.prefs

import kotlin.time.Duration.Companion.hours

object Prefs {
    const val GROUP = "amznkiller"
    private val STALE_THRESHOLD = 24.hours
    val STALE_THRESHOLD_MS = STALE_THRESHOLD.inWholeMilliseconds

    val SELECTOR_URL =
        stringPref(
            "selector_url",
            "https://raw.githubusercontent.com/hxreborn/amznkiller/main/lists/generated/merged.txt",
        )
    val CACHED_SELECTORS = stringPref("cached_selectors", "")
    val LAST_FETCHED = longPref("last_fetched", 0L)
    val DEBUG_LOGS = boolPref("debug_logs", false)
    val INJECTION_ENABLED = boolPref("injection_enabled", true)
    val WEBVIEW_DEBUGGING = boolPref("webview_debugging", false)
    val FORCE_DARK_WEBVIEW = boolPref("force_dark_webview", false)
    val FORCE_DARK_MODE = stringPref("force_dark_mode", ForceDarkMode.OFF.prefValue)
    val PRICE_CHARTS_ENABLED = boolPref("price_charts_enabled", false)
    val DISABLE_VIDEO_AUTOPLAY = boolPref("disable_video_autoplay", true)

    val HIDE_RUFUS = boolPref("hide_rufus", false)
    val LAST_REFRESH_FAILED = boolPref("last_refresh_failed", false)
    val AUTO_UPDATE = boolPref("auto_update", true)

    val DARK_THEME_CONFIG = stringPref("dark_theme_config", "follow_system")
    val USE_DYNAMIC_COLOR = boolPref("use_dynamic_color", true)

    val all: List<PrefSpec<*>> =
        listOf(
            SELECTOR_URL,
            CACHED_SELECTORS,
            LAST_FETCHED,
            HIDE_RUFUS,
            LAST_REFRESH_FAILED,
            DEBUG_LOGS,
            INJECTION_ENABLED,
            WEBVIEW_DEBUGGING,
            FORCE_DARK_WEBVIEW,
            FORCE_DARK_MODE,
            PRICE_CHARTS_ENABLED,
            DISABLE_VIDEO_AUTOPLAY,
            AUTO_UPDATE,
            DARK_THEME_CONFIG,
            USE_DYNAMIC_COLOR,
        )

    fun parseSelectors(raw: String): List<String> = raw.lines().filter { it.isNotBlank() }

    fun readForceDarkMode(prefs: android.content.SharedPreferences): ForceDarkMode =
        if (prefs.contains(FORCE_DARK_MODE.key)) {
            ForceDarkMode.fromPrefValue(FORCE_DARK_MODE.read(prefs))
        } else if (FORCE_DARK_WEBVIEW.read(prefs)) {
            ForceDarkMode.ON
        } else {
            ForceDarkMode.OFF
        }
}
