package eu.hxreborn.amznkiller.ui.preview

import android.app.Application
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object PreviewFixtures {
    val dashboardReady: DashboardUiState.Ready =
        DashboardUiState.Ready(
            isXposedActive = true,
            frameworkVersion = "LSPosed v1.11.0",
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = false,
            selectorCount = 42,
            lastFetched = 1_710_000_000_000L,
            injectionEnabled = true,
            lastRefreshOutcome = null,
        )

    val settingsReady: SettingsUiState.Ready =
        SettingsUiState.Ready(
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = true,
            debugLogs = false,
            injectionEnabled = true,
            forceDarkMode = ForceDarkMode.FOLLOW_SYSTEM,
        )
}

internal class FakeAppViewModel(
    application: Application,
    dashboardState: DashboardUiState = PreviewFixtures.dashboardReady,
    settingsState: SettingsUiState = PreviewFixtures.settingsReady,
) : AppViewModel(
        application,
        repositoryProvider = { error("preview FakeAppViewModel should not access repository") },
    ) {
    override val dashboardUiState: StateFlow<DashboardUiState> =
        MutableStateFlow(dashboardState).asStateFlow()

    override val settingsUiState: StateFlow<SettingsUiState> =
        MutableStateFlow(settingsState).asStateFlow()

    override fun refreshAll() = Unit

    override fun triggerAutoUpdateIfEnabled() = Unit

    override fun setXposedActive(
        active: Boolean,
        frameworkVersion: String?,
    ) = Unit

    override fun <T> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) = Unit

    override fun setLauncherIconHidden(hidden: Boolean) = Unit
}
