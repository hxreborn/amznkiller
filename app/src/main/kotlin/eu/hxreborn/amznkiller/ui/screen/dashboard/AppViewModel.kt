package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import eu.hxreborn.amznkiller.prefs.PrefSpec
import kotlinx.coroutines.flow.StateFlow

abstract class AppViewModel : ViewModel() {
    abstract val uiState: StateFlow<FilterUiState>

    abstract fun refreshAll()

    abstract fun setXposedActive(
        active: Boolean,
        frameworkVersion: String? = null,
        frameworkPrivilege: String? = null,
    )

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )
}
