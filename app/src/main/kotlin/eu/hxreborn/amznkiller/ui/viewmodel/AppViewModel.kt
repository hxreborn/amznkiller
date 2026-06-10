package eu.hxreborn.amznkiller.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eu.hxreborn.amznkiller.App
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.XposedState
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.prefs.PrefsRepository
import eu.hxreborn.amznkiller.selectors.MergeResult
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SelectorSyncEvent
import eu.hxreborn.amznkiller.ui.state.SelectorSyncOutcome
import eu.hxreborn.amznkiller.ui.state.SettingsUiState
import eu.hxreborn.amznkiller.util.LauncherIconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Loading as DashboardLoading
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Ready as DashboardReady
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Loading as SettingsLoading
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Ready as SettingsReady

private val MIN_REFRESH_DISPLAY = 1000.milliseconds

@Stable
open class AppViewModel(
    private val application: Application,
    repositoryProvider: () -> PrefsRepository = { App.from(application).prefsRepository },
    xposedStateProvider: () -> StateFlow<XposedState> = { App.from(application).xposedState },
) : ViewModel() {
    private val repository: PrefsRepository by lazy(repositoryProvider)
    private val xposedState: StateFlow<XposedState> by lazy(xposedStateProvider)

    private val refreshing = MutableStateFlow(false)
    private val lastRefreshOutcome = MutableStateFlow<SelectorSyncOutcome?>(null)
    private val launcherIconHidden = MutableStateFlow(false)

    @Volatile private var autoUpdateTriggered = false

    init {
        viewModelScope.launch {
            runCatching {
                launcherIconHidden.value = !LauncherIconHelper.isLauncherIconVisible(application)
            }
        }
    }

    private val _dashboardUiState by lazy {
        combine(
            repository.state,
            refreshing,
            xposedState,
            lastRefreshOutcome,
        ) { prefs, isRefreshing, xposed, outcome ->
            DashboardReady(
                isXposedActive = xposed.active,
                frameworkVersion = xposed.frameworkVersion,
                isRefreshing = isRefreshing,
                isRefreshFailed = prefs.isRefreshFailed,
                isStale = prefs.isStale,
                lastFetched = prefs.lastFetched,
                selectorCount = prefs.selectorCount,
                injectionEnabled = prefs.injectionEnabled,
                lastRefreshOutcome = outcome,
            )
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = DashboardLoading,
        )
    }

    open val dashboardUiState: StateFlow<DashboardUiState>
        get() = _dashboardUiState

    private val _settingsUiState by lazy {
        combine(
            repository.state,
            launcherIconHidden,
        ) { prefs, iconHidden ->
            SettingsReady(
                selectorUrl = prefs.selectorUrl,
                autoUpdate = prefs.autoUpdate,
                injectionEnabled = prefs.injectionEnabled,
                debugLogs = prefs.debugLogs,
                webviewDebugging = prefs.webviewDebugging,
                forceDarkMode = prefs.forceDarkMode,
                priceChartsEnabled = prefs.priceChartsEnabled,
                hideRufus = prefs.hideRufus,
                darkThemeConfig = prefs.darkThemeConfig,
                useDynamicColor = prefs.useDynamicColor,
                isLauncherIconHidden = iconHidden,
            )
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = SettingsLoading,
        )
    }

    open val settingsUiState: StateFlow<SettingsUiState>
        get() = _settingsUiState

    open fun refreshAll() {
        if (refreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            refreshing.value = true
            val mark = TimeSource.Monotonic.markNow()
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
                val remaining = MIN_REFRESH_DISPLAY - mark.elapsedNow()
                if (remaining.isPositive()) delay(remaining)
                refreshing.value = false
            }
        }
    }

    open fun triggerAutoUpdateIfEnabled() {
        if (autoUpdateTriggered || !repository.autoUpdate) return
        autoUpdateTriggered = true
        refreshAll()
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

    open fun <T> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
        viewModelScope.launch(Dispatchers.IO) { repository.save(pref, value) }
    }

    open fun setLauncherIconHidden(hidden: Boolean) {
        LauncherIconHelper.setLauncherIconVisible(application, !hidden)
        launcherIconHidden.value = hidden
    }

    companion object {
        val Factory =
            viewModelFactory {
                initializer { AppViewModel(this[APPLICATION_KEY] as Application) }
            }
    }
}
