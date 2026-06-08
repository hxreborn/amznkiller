package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.amznkiller.ui.state.AppPrefs
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PrefsRepository(
    private val local: SharedPreferences,
    private val remoteProvider: () -> SharedPreferences?,
) {
    val state: Flow<AppPrefs> =
        callbackFlow {
            fun emit() {
                val raw = Prefs.CACHED_SELECTORS.read(local)
                val selectors = Prefs.parseSelectors(raw)
                val lastFetched = Prefs.LAST_FETCHED.read(local)
                trySend(
                    AppPrefs(
                        selectorCount = selectors.size,
                        selectorUrl = Prefs.SELECTOR_URL.read(local),
                        lastFetched = lastFetched,
                        debugLogs = Prefs.DEBUG_LOGS.read(local),
                        injectionEnabled = Prefs.INJECTION_ENABLED.read(local),
                        webviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(local),
                        forceDarkMode = Prefs.readForceDarkMode(local),
                        priceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(local),
                        hideRufus = Prefs.HIDE_RUFUS.read(local),
                        autoUpdate = Prefs.AUTO_UPDATE.read(local),
                        isStale =
                            lastFetched == 0L ||
                                System.currentTimeMillis() - lastFetched > Prefs.STALE_THRESHOLD_MS,
                        isRefreshFailed = Prefs.LAST_REFRESH_FAILED.read(local),
                        darkThemeConfig =
                            runCatching {
                                DarkThemeConfig.valueOf(
                                    Prefs.DARK_THEME_CONFIG.read(local).uppercase(),
                                )
                            }.getOrDefault(DarkThemeConfig.FOLLOW_SYSTEM),
                        useDynamicColor = Prefs.USE_DYNAMIC_COLOR.read(local),
                    ),
                )
            }

            emit()
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> emit() }
            local.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { local.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    val currentSelectors: List<String>
        get() = Prefs.parseSelectors(Prefs.CACHED_SELECTORS.read(local))

    val selectorUrl: String
        get() = Prefs.SELECTOR_URL.read(local)

    val autoUpdate: Boolean
        get() = Prefs.AUTO_UPDATE.read(local)

    fun <T> save(
        pref: PrefSpec<T>,
        value: T,
    ) {
        local.edit { pref.write(this, value) }
        runCatching { remoteProvider()?.edit(commit = true) { pref.write(this, value) } }
    }

    fun syncToRemote() {
        val remote = remoteProvider() ?: return
        runCatching {
            remote.edit(commit = true) {
                Prefs.all.forEach { it.copyIfChanged(local, remote, this) }
            }
        }
    }
}
