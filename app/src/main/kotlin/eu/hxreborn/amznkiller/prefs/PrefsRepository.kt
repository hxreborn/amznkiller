package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences
import eu.hxreborn.amznkiller.ui.state.FilterPrefsState
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface PrefsRepository {
    val state: Flow<FilterPrefsState>

    fun <T : Any> save(
        pref: PrefSpec<T>,
        value: T,
    )

    fun getCurrentSelectors(): List<String>

    fun getSelectorUrl(): String

    fun syncLocalToRemote()
}

class PrefsRepositoryImpl(
    private val localPrefs: SharedPreferences,
    private val remotePrefsProvider: () -> SharedPreferences?,
) : PrefsRepository {
    override val state: Flow<FilterPrefsState> =
        callbackFlow {
            fun emit() {
                val raw = Prefs.CACHED_SELECTORS.read(localPrefs)
                val selectors = Prefs.parseSelectors(raw)
                trySend(
                    FilterPrefsState(
                        cachedSelectors = selectors,
                        selectorCount = selectors.size,
                        selectorUrl = Prefs.SELECTOR_URL.read(localPrefs),
                        lastFetched = Prefs.LAST_FETCHED.read(localPrefs),
                        debugLogs = Prefs.DEBUG_LOGS.read(localPrefs),
                        injectionEnabled = Prefs.INJECTION_ENABLED.read(localPrefs),
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

            emit()
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> emit() }
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

    override fun getCurrentSelectors(): List<String> =
        Prefs.parseSelectors(Prefs.CACHED_SELECTORS.read(localPrefs))

    override fun getSelectorUrl(): String = Prefs.SELECTOR_URL.read(localPrefs)

    override fun syncLocalToRemote() {
        val remote = remotePrefsProvider() ?: return
        val editor = remote.edit()
        Prefs.all.forEach { it.copyTo(localPrefs, editor) }
        editor.apply()
    }
}
