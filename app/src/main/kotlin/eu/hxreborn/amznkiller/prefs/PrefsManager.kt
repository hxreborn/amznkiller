package eu.hxreborn.amznkiller.prefs

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
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
    val hideRufus: Boolean,
)

object PrefsManager {
    @Volatile
    var remotePrefs: SharedPreferences? = null
        private set

    @Volatile
    private var cachedSelectors: List<String> = emptyList()

    val selectors: List<String>
        get() = cachedSelectors

    @Volatile
    private var cachedDebugLogs: Boolean = Prefs.DEBUG_LOGS.default

    val debugLogs: Boolean
        get() = cachedDebugLogs

    @Volatile
    private var cachedInjectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default

    val injectionEnabled: Boolean
        get() = cachedInjectionEnabled

    @Volatile
    private var cachedWebviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default

    val webviewDebugging: Boolean
        get() = cachedWebviewDebugging

    @Volatile
    private var cachedForceDarkMode: ForceDarkMode = ForceDarkMode.OFF

    val forceDarkMode: ForceDarkMode
        get() = cachedForceDarkMode

    val forceDarkWebview: Boolean
        get() = cachedForceDarkMode.isActive(systemInDarkMode())

    @Volatile
    private var cachedPriceChartsEnabled: Boolean = Prefs.PRICE_CHARTS_ENABLED.default

    val priceChartsEnabled: Boolean
        get() = cachedPriceChartsEnabled

    @Volatile
    private var cachedHideRufus: Boolean = Prefs.HIDE_RUFUS.default

    val hideRufus: Boolean
        get() = cachedHideRufus

    fun init(xposed: XposedInterface) {
        runCatching {
            remotePrefs = xposed.getRemotePreferences(Prefs.GROUP)
            refreshCache()
            Logger.debug { "PrefsManager initialized" }
        }.onFailure { Logger.log(Log.ERROR, "PrefsManager.init() failed", it) }
    }

    private fun refreshCache() {
        runCatching {
            remotePrefs?.let { prefs ->
                val raw = Prefs.CACHED_SELECTORS.read(prefs)
                cachedSelectors = SelectorSanitizer.sanitize(raw.lineSequence())
                cachedDebugLogs = Prefs.DEBUG_LOGS.read(prefs)
                cachedInjectionEnabled = Prefs.INJECTION_ENABLED.read(prefs)
                cachedWebviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(prefs)
                cachedForceDarkMode = Prefs.readForceDarkMode(prefs)
                cachedPriceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(prefs)
                cachedHideRufus = Prefs.HIDE_RUFUS.read(prefs)
            }
        }.onFailure { Logger.log(Log.ERROR, "refreshCache() failed", it) }
    }

    fun snapshot(): PrefsSnapshot {
        refreshCache()
        val darkMode = cachedForceDarkMode
        return PrefsSnapshot(
            selectors = cachedSelectors,
            injectionEnabled = cachedInjectionEnabled,
            webviewDebugging = cachedWebviewDebugging,
            forceDarkMode = darkMode,
            forceDarkWebview = darkMode.isActive(systemInDarkMode()),
            priceChartsEnabled = cachedPriceChartsEnabled,
            hideRufus = cachedHideRufus,
        )
    }

    private fun systemInDarkMode(): Boolean {
        val uiMode = currentApplication()?.resources?.configuration?.uiMode ?: return false
        return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun setFallbackSelectors(fallback: List<String>) {
        cachedSelectors = fallback
    }

    fun isStale(): Boolean {
        val fetched =
            runCatching {
                remotePrefs?.let { Prefs.LAST_FETCHED.read(it) }
            }.getOrNull() ?: 0L
        return System.currentTimeMillis() - fetched > Prefs.STALE_THRESHOLD_MS
    }

    private inline fun <T> readRemote(
        fallback: T,
        read: (SharedPreferences) -> T,
        cache: (T) -> Unit,
    ): T {
        val value =
            runCatching {
                remotePrefs?.let(read)
            }.getOrNull() ?: return fallback
        cache(value)
        return value
    }
}

private fun currentApplication(): Application? =
    runCatching {
        Class
            .forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Application
    }.getOrNull()
