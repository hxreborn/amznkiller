package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import eu.hxreborn.amznkiller.ui.state.AppPrefsState

@Stable
sealed interface AppUiState {
    data object Loading : AppUiState

    @Immutable
    data class Success(
        val prefs: AppPrefsState,
    ) : AppUiState
}
