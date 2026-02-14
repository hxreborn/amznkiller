package eu.hxreborn.amznkiller.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.prefs.PrefsRepository
import eu.hxreborn.amznkiller.selectors.MergeResult
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.ui.state.AppPrefsState
import eu.hxreborn.amznkiller.ui.state.AppUiState
import eu.hxreborn.amznkiller.ui.state.SelectorSyncEvent
import eu.hxreborn.amznkiller.ui.state.SelectorSyncOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AppViewModelImpl(
    private val repository: PrefsRepository,
) : AppViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val xposedActive = MutableStateFlow(false)
    private val frameworkVersion = MutableStateFlow<String?>(null)
    private val lastRefreshOutcome = MutableStateFlow<SelectorSyncOutcome?>(null)

    override val uiState: StateFlow<AppUiState> =
        combine(
            repository.state,
            refreshing,
            xposedActive,
            lastRefreshOutcome,
            frameworkVersion,
        ) { prefs: AppPrefsState, isRefreshing: Boolean, active: Boolean, outcome: SelectorSyncOutcome?, fwVersion: String? ->
            AppUiState.Success(
                prefs.copy(
                    isXposedActive = active,
                    frameworkVersion = fwVersion,
                    isRefreshing = isRefreshing,
                    lastRefreshOutcome = outcome,
                ),
            )
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = AppUiState.Loading,
        )

    override fun refreshAll() {
        if (refreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            refreshing.value = true
            try {
                repository.save(Prefs.LAST_REFRESH_FAILED, false)
                val oldSelectors = repository.currentSelectors.toSet()
                runCatching {
                    val url = repository.selectorUrl
                    val result = SelectorUpdater.fetchMerged(url)
                    if (result.selectors.isEmpty()) {
                        emitFailure(R.string.snackbar_no_selectors)
                        return@runCatching
                    }
                    when (result) {
                        is MergeResult.Partial -> {
                            val merged = result.selectors.sorted().joinToString("\n")
                            repository.save(Prefs.CACHED_SELECTORS, merged)
                            emitFailure(R.string.snackbar_fetch_failed_bundled)
                        }

                        is MergeResult.Success -> {
                            val added = (result.selectors - oldSelectors).size
                            val removed = (oldSelectors - result.selectors).size
                            val merged = result.selectors.sorted().joinToString("\n")
                            repository.save(Prefs.CACHED_SELECTORS, merged)
                            repository.save(Prefs.LAST_FETCHED, System.currentTimeMillis())
                            repository.save(Prefs.LAST_REFRESH_FAILED, false)
                            lastRefreshOutcome.value =
                                SelectorSyncOutcome(
                                    if (added == 0 && removed == 0) {
                                        SelectorSyncEvent.UpToDate
                                    } else {
                                        SelectorSyncEvent.Updated(added, removed)
                                    },
                                )
                        }
                    }
                }.onFailure {
                    emitFailure(R.string.snackbar_update_failed, fallback = it.message)
                }
            } finally {
                refreshing.value = false
            }
        }
    }

    private fun emitFailure(
        messageResId: Int,
        fallback: String? = null,
    ) {
        repository.save(Prefs.LAST_REFRESH_FAILED, true)
        lastRefreshOutcome.value =
            SelectorSyncOutcome(
                SelectorSyncEvent.Error(messageResId = messageResId, fallback = fallback),
            )
    }

    override fun setXposedActive(
        active: Boolean,
        frameworkVersion: String?,
    ) {
        xposedActive.value = active
        this.frameworkVersion.value = frameworkVersion
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
        repository.save(pref, value)
    }
}

class AppViewModelFactory(
    private val repository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModelImpl(repository) as T
}
