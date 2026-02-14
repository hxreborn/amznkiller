package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.amznkiller.ui.state.AppPrefsState
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface PrefsRepository {
    val state: Flow<AppPrefsState>
    val currentSelectors: List<String>
    val selectorUrl: String

    fun <T : Any> save(
        pref: PrefSpec<T>,
        value: T,
    )

    fun syncLocalToRemote()
}

class PrefsRepositoryImpl(
    private val localPrefs: SharedPreferences,
    private val remotePrefsProvider: () -> SharedPreferences?,
) : PrefsRepository {
    override val state: Flow<AppPrefsState> =
        callbackFlow {
            fun sendState() {
                val raw = Prefs.CACHED_SELECTORS.read(localPrefs)
                val selectors = Prefs.parseSelectors(raw)
                val lastFetched = Prefs.LAST_FETCHED.read(localPrefs)
                trySend(
                    AppPrefsState(
                        cachedSelectors = selectors,
                        selectorCount = selectors.size,
                        selectorUrl = Prefs.SELECTOR_URL.read(localPrefs),
                        lastFetched = lastFetched,
                        debugLogs = Prefs.DEBUG_LOGS.read(localPrefs),
                        injectionEnabled = Prefs.INJECTION_ENABLED.read(localPrefs),
                        webviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(localPrefs),
                        forceDarkWebview = Prefs.FORCE_DARK_WEBVIEW.read(localPrefs),
                        priceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(localPrefs),
                        autoUpdate = Prefs.AUTO_UPDATE.read(localPrefs),
                        isStale =
                            lastFetched == 0L ||
                                System.currentTimeMillis() - lastFetched > Prefs.STALE_THRESHOLD_MS,
                        isRefreshFailed = Prefs.LAST_REFRESH_FAILED.read(localPrefs),
                        darkThemeConfig =
                            runCatching {
                                DarkThemeConfig.valueOf(
                                    Prefs.DARK_THEME_CONFIG.read(localPrefs).uppercase(),
                                )
                            }.getOrDefault(DarkThemeConfig.FOLLOW_SYSTEM),
                        useDynamicColor = Prefs.USE_DYNAMIC_COLOR.read(localPrefs),
                    ),
                )
            }

            sendState()
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener {
                    _,
                    _,
                    ->
                    sendState()
                }
            localPrefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { localPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    override fun <T : Any> save(
        pref: PrefSpec<T>,
        value: T,
    ) {
        localPrefs.edit().also { pref.write(it, value) }.apply()
        remotePrefsProvider()?.edit()?.also { pref.write(it, value) }?.apply()
    }

    override val currentSelectors: List<String>
        get() = Prefs.parseSelectors(Prefs.CACHED_SELECTORS.read(localPrefs))

    override val selectorUrl: String
        get() = Prefs.SELECTOR_URL.read(localPrefs)

    override fun syncLocalToRemote() {
        val remote = remotePrefsProvider() ?: return
        remote.edit {
            Prefs.all.forEach { it.copyTo(localPrefs, this) }
        }
    }
}
