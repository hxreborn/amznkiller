package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import eu.hxreborn.amznkiller.ui.state.FilterPrefsState

@Stable
sealed interface FilterUiState {
    data object Loading : FilterUiState

    @Immutable
    data class Success(
        val prefs: FilterPrefsState,
    ) : FilterUiState
}
