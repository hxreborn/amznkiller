package eu.hxreborn.amznkiller.xposed.hook

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.selectors.SelectorSanitizer
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

// Cached snapshot of remote prefs, refreshed by the change listener registered in
// installHookPrefs(). Hook process reads these on every intercept; getRemotePreferences()
// is a synchronous Binder IPC and must not run on a hot path. Writes happen only in
// loadHookPrefs() and setFallbackSelectors(); the hook process is read-only on remote
// prefs (edit() silently fails from a hooked process).

@Volatile internal var cachedSelectors: List<String> = emptyList()

@Volatile internal var cachedDebugLogs: Boolean = Prefs.DEBUG_LOGS.default

@Volatile internal var cachedInjectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default

@Volatile internal var cachedWebviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default

@Volatile internal var cachedForceDarkMode: ForceDarkMode = ForceDarkMode.OFF

@Volatile internal var cachedPriceChartsEnabled: Boolean = Prefs.PRICE_CHARTS_ENABLED.default

@Volatile internal var cachedHideRufus: Boolean = Prefs.HIDE_RUFUS.default

@Volatile private var remotePrefs: SharedPreferences? = null

@Volatile private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

internal val forceDarkWebview: Boolean
    get() = cachedForceDarkMode.isActive(systemInDarkMode())

internal fun setFallbackSelectors(fallback: List<String>) {
    cachedSelectors = fallback
}

internal fun installHookPrefs(xposed: XposedInterface) {
    runCatching {
        remotePrefs?.unregisterOnSharedPreferenceChangeListener(prefsListener)
        val prefs = xposed.getRemotePreferences(Prefs.GROUP)
        remotePrefs = prefs
        loadHookPrefs(prefs)
        val l =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                runCatching { loadHookPrefs(prefs) }
                    .onFailure { Logger.log(Log.ERROR, "prefs reload key=$key", it) }
            }
        prefsListener = l
        prefs.registerOnSharedPreferenceChangeListener(l)
        Logger.log(Log.INFO, "prefs ready count=${cachedSelectors.size}")
    }.onFailure { Logger.log(Log.ERROR, "prefs init", it) }
}

internal fun loadHookPrefs(prefs: SharedPreferences) {
    runCatching {
        val raw = Prefs.CACHED_SELECTORS.read(prefs)
        cachedSelectors = SelectorSanitizer.sanitize(raw.lineSequence())
        cachedDebugLogs = Prefs.DEBUG_LOGS.read(prefs)
        cachedInjectionEnabled = Prefs.INJECTION_ENABLED.read(prefs)
        cachedWebviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(prefs)
        cachedForceDarkMode = Prefs.readForceDarkMode(prefs)
        cachedPriceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(prefs)
        cachedHideRufus = Prefs.HIDE_RUFUS.read(prefs)
    }.onFailure { Logger.log(Log.ERROR, "prefs refresh", it) }
}

private fun systemInDarkMode(): Boolean {
    val uiMode = currentApplication()?.resources?.configuration?.uiMode ?: return false
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

private fun currentApplication(): Application? =
    runCatching {
        Class
            .forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Application
    }.getOrNull()
