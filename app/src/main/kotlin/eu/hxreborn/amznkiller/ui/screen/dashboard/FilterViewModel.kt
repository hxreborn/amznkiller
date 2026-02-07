package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class FilterViewModel : ViewModel() {
    abstract val uiState: StateFlow<FilterUiState>
    abstract val updateEvents: SharedFlow<UpdateEvent>

    abstract fun refreshAll()

    abstract fun setXposedActive(active: Boolean)

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )
}
