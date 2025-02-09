package eu.hxreborn.amznkiller.ui.viewmodel

import androidx.lifecycle.ViewModel
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.state.AppUiState
import kotlinx.coroutines.flow.StateFlow

abstract class AppViewModel : ViewModel() {
    abstract val uiState: StateFlow<AppUiState>

    abstract fun refreshAll()

    abstract fun setXposedActive(
        active: Boolean,
        frameworkVersion: String? = null,
    )

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )
}
