package eu.hxreborn.amznkiller.ui.state

import androidx.compose.runtime.Immutable
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    @Immutable
    data class Ready(
        val selectorUrl: String = Prefs.SELECTOR_URL.default,
        val autoUpdate: Boolean = Prefs.AUTO_UPDATE.default,
        val injectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default,
        val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
        val webviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default,
        val forceDarkMode: ForceDarkMode = ForceDarkMode.OFF,
        val priceChartsEnabled: Boolean = Prefs.PRICE_CHARTS_ENABLED.default,
        val hideRufus: Boolean = Prefs.HIDE_RUFUS.default,
        val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        val useDynamicColor: Boolean = Prefs.USE_DYNAMIC_COLOR.default,
        val isLauncherIconHidden: Boolean = false,
    ) : SettingsUiState
}
