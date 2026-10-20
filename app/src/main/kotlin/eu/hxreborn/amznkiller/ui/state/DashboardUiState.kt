package eu.hxreborn.amznkiller.ui.state

import androidx.compose.runtime.Immutable
import eu.hxreborn.amznkiller.prefs.ForceDarkMode

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    @Immutable
    data class Ready(
        val isXposedActive: Boolean,
        val isRefreshing: Boolean,
        val isRefreshFailed: Boolean,
        val isStale: Boolean,
        val lastFetched: Long,
        val selectorCount: Int,
        val injectionEnabled: Boolean,
        val priceChartKeepa: Boolean,
        val priceChartCamel: Boolean,
        val disableVideoAutoplay: Boolean,
        val hideRufus: Boolean,
        val forceDarkMode: ForceDarkMode,
        val lastRefreshOutcome: SelectorSyncOutcome?,
    ) : DashboardUiState
}
