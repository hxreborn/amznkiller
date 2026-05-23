package eu.hxreborn.amznkiller.ui.viewmodel

import androidx.lifecycle.ViewModel
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState
import kotlinx.coroutines.flow.StateFlow

abstract class AppViewModel : ViewModel() {
    abstract val dashboardUiState: StateFlow<DashboardUiState>
    abstract val settingsUiState: StateFlow<SettingsUiState>

    abstract fun refreshAll()

    abstract fun triggerAutoUpdateIfEnabled()

    abstract fun setXposedActive(
        active: Boolean,
        frameworkVersion: String? = null,
    )

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )

    abstract fun setLauncherIconHidden(hidden: Boolean)

    abstract fun syncLocalToRemote()
}
