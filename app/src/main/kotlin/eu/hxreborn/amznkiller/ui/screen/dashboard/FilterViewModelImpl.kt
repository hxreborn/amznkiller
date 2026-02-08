package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.prefs.PrefsRepository
import eu.hxreborn.amznkiller.selectors.MergeResult
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.ui.state.RefreshOutcome
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class FilterViewModelImpl(
    private val repository: PrefsRepository,
) : FilterViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val lastRefreshFailed = MutableStateFlow(false)
    private val xposedActive = MutableStateFlow(false)
    private val lastRefreshOutcome = MutableStateFlow<RefreshOutcome?>(null)

    override val uiState: StateFlow<FilterUiState> =
        combine(
            repository.state,
            refreshing,
            xposedActive,
            lastRefreshFailed,
            lastRefreshOutcome,
        ) { flows ->
            @Suppress("UNCHECKED_CAST")
            val prefs = flows[0] as eu.hxreborn.amznkiller.ui.state.FilterPrefsState
            val isRefreshing = flows[1] as Boolean
            val active = flows[2] as Boolean
            val failed = flows[3] as Boolean
            val outcome = flows[4] as RefreshOutcome?
            FilterUiState.Success(
                prefs.copy(
                    isXposedActive = active,
                    isRefreshing = isRefreshing,
                    isRefreshFailed = failed,
                    lastRefreshOutcome = outcome,
                ),
            )
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = FilterUiState.Loading,
        )

    override fun refreshAll() {
        if (refreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            refreshing.value = true
            lastRefreshFailed.value = false
            val oldSelectors = repository.getCurrentSelectors().toSet()
            runCatching {
                val url = repository.getSelectorUrl()
                val result = SelectorUpdater.fetchMerged(url)
                if (result.selectors.isEmpty()) {
                    lastRefreshFailed.value = true
                    lastRefreshOutcome.value =
                        RefreshOutcome(UpdateEvent.Error("No selectors fetched"))
                    return@runCatching
                }
                when (result) {
                    is MergeResult.Partial -> {
                        lastRefreshFailed.value = true
                        lastRefreshOutcome.value =
                            RefreshOutcome(
                                UpdateEvent.Error("Remote fetch failed, using embedded"),
                            )
                    }

                    is MergeResult.Success -> {
                        val added = (result.selectors - oldSelectors).size
                        val removed = (oldSelectors - result.selectors).size
                        val merged = result.selectors.sorted().joinToString("\n")
                        repository.save(Prefs.CACHED_SELECTORS, merged)
                        repository.save(Prefs.LAST_FETCHED, System.currentTimeMillis())
                        lastRefreshOutcome.value =
                            RefreshOutcome(
                                if (added == 0 && removed == 0) {
                                    UpdateEvent.UpToDate
                                } else {
                                    UpdateEvent.Updated(added, removed)
                                },
                            )
                    }
                }
            }.onFailure {
                lastRefreshFailed.value = true
                lastRefreshOutcome.value =
                    RefreshOutcome(UpdateEvent.Error(it.message ?: "Update failed"))
            }
            refreshing.value = false
        }
    }

    override fun setXposedActive(active: Boolean) {
        xposedActive.value = active
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
        repository.save(pref, value)
    }
}

class FilterViewModelFactory(
    private val repository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = FilterViewModelImpl(repository) as T
}
