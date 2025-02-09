package eu.hxreborn.amznkiller.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface AppUiState {
    data object Loading : AppUiState

    @Immutable
    data class Success(
        val prefs: AppPrefsState,
    ) : AppUiState
}
