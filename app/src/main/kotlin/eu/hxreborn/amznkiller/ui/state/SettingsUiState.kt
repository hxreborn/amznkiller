package eu.hxreborn.amznkiller.ui.state

import androidx.compose.runtime.Immutable
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    @Immutable
    data class Ready(
        val selectorUrl: String = Prefs.SELECTOR_URL.default,
        val autoUpdate: Boolean = Prefs.AUTO_UPDATE.default,
        val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
        val webviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default,
        val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        val useDynamicColor: Boolean = Prefs.USE_DYNAMIC_COLOR.default,
        val isLauncherIconHidden: Boolean = false,
    ) : SettingsUiState
}
