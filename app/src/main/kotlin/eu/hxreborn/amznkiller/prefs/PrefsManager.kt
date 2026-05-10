package eu.hxreborn.amznkiller.prefs

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import eu.hxreborn.amznkiller.selectors.SelectorSanitizer
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

data class PrefsSnapshot(
    val selectors: List<String>,
    val injectionEnabled: Boolean,
    val webviewDebugging: Boolean,
    val forceDarkMode: ForceDarkMode,
    val forceDarkWebview: Boolean,
    val priceChartsEnabled: Boolean,
)

object PrefsManager {
    @Volatile
    var remotePrefs: SharedPreferences? = null
        private set

    @Volatile
    var selectors: List<String> = emptyList()
        private set

    @Volatile
    var debugLogs: Boolean = Prefs.DEBUG_LOGS.default
        private set

    @Volatile
    var injectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default
        private set

    @Volatile
    var webviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default
        private set

    @Volatile
    var forceDarkMode: ForceDarkMode = ForceDarkMode.OFF
        private set

    val forceDarkWebview: Boolean
        get() = snapshot().forceDarkWebview

    @Volatile
    var priceChartsEnabled: Boolean = Prefs.PRICE_CHARTS_ENABLED.default
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
                selectors = SelectorSanitizer.sanitize(raw.lineSequence())
                debugLogs = Prefs.DEBUG_LOGS.read(prefs)
                injectionEnabled = Prefs.INJECTION_ENABLED.read(prefs)
                webviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(prefs)
                forceDarkMode = Prefs.readForceDarkMode(prefs)
                priceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(prefs)
            }
        }.onFailure { Logger.log("refreshCache() failed", it) }
    }

    fun snapshot() =
        PrefsSnapshot(
            selectors = selectors,
            injectionEnabled = injectionEnabled,
            webviewDebugging = webviewDebugging,
            forceDarkMode = forceDarkMode,
            forceDarkWebview = forceDarkMode.isActive(systemInDarkMode()),
            priceChartsEnabled = priceChartsEnabled,
        )

    private fun systemInDarkMode(): Boolean {
        val uiMode = currentApplication()?.resources?.configuration?.uiMode ?: return false
        return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun setFallbackSelectors(fallback: List<String>) {
        selectors = fallback
    }

    fun isStale(): Boolean {
        val fetched = remotePrefs?.let { Prefs.LAST_FETCHED.read(it) } ?: 0L
        return System.currentTimeMillis() - fetched > Prefs.STALE_THRESHOLD_MS
    }
}

private fun currentApplication(): Application? =
    runCatching {
        Class
            .forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Application
    }.getOrNull()
