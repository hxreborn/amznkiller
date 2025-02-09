package eu.hxreborn.amznkiller.ui.state

import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig

data class AppPrefsState(
    val cachedSelectors: List<String> = emptyList(),
    val selectorCount: Int = 0,
    val selectorUrl: String = Prefs.SELECTOR_URL.default,
    val lastFetched: Long = 0L,
    val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
    val injectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default,
    val webviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default,
    val autoUpdate: Boolean = Prefs.AUTO_UPDATE.default,
    val isStale: Boolean = true,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = Prefs.USE_DYNAMIC_COLOR.default,
    val isXposedActive: Boolean = false,
    val frameworkVersion: String? = null,
    val isRefreshing: Boolean = false,
    val isRefreshFailed: Boolean = false,
    val lastRefreshOutcome: SelectorSyncOutcome? = null,
)
