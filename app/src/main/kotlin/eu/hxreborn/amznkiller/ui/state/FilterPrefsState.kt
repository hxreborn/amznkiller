package eu.hxreborn.amznkiller.ui.state

import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig

data class FilterPrefsState(
    val cachedSelectors: List<String> = emptyList(),
    val selectorCount: Int = 0,
    val lastFetched: Long = 0L,
    val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = Prefs.USE_DYNAMIC_COLOR.default,
    val isXposedActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val isRefreshFailed: Boolean = false,
)
