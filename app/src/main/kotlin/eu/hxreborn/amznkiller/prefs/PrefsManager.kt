package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

object PrefsManager {
    @Volatile
    var remotePrefs: SharedPreferences? = null
        private set

    @Volatile
    var selectors: List<String> = emptyList()

    @Volatile
    var debugLogs: Boolean = Prefs.DEBUG_LOGS.default
        private set

    fun init(xposed: XposedInterface) {
        runCatching {
            remotePrefs = xposed.getRemotePreferences(Prefs.GROUP)
            refreshCache()
            remotePrefs?.registerOnSharedPreferenceChangeListener { _, _ ->
                refreshCache()
            }
            Logger.log("PrefsManager initialized")
        }.onFailure { Logger.log("PrefsManager.init() failed", it) }
    }

    private fun refreshCache() {
        runCatching {
            remotePrefs?.let { prefs ->
                val raw = Prefs.CACHED_SELECTORS.read(prefs)
                selectors = raw.lines().filter { it.isNotBlank() }
                debugLogs = Prefs.DEBUG_LOGS.read(prefs)
            }
        }.onFailure { Logger.log("refreshCache() failed", it) }
    }

    fun isStale(): Boolean {
        val fetched = remotePrefs?.let { Prefs.LAST_FETCHED.read(it) } ?: 0L
        return System.currentTimeMillis() - fetched > Prefs.STALE_THRESHOLD_MS
    }
}
