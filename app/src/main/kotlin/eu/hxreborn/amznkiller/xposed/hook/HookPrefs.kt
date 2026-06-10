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

@Volatile internal var selectors: List<String> = emptyList()

@Volatile internal var debugLogs: Boolean = Prefs.DEBUG_LOGS.default

@Volatile internal var injectionEnabled: Boolean = Prefs.INJECTION_ENABLED.default

@Volatile internal var webviewDebugging: Boolean = Prefs.WEBVIEW_DEBUGGING.default

@Volatile internal var forceDarkMode: ForceDarkMode = ForceDarkMode.OFF

@Volatile internal var priceChartsEnabled: Boolean = Prefs.PRICE_CHARTS_ENABLED.default

@Volatile internal var hideRufus: Boolean = Prefs.HIDE_RUFUS.default

@Volatile private var remotePrefs: SharedPreferences? = null

@Volatile private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

// hot path, read on every hooked call, so OFF and ON skip the config lookup
internal val forceDarkWebview: Boolean
    get() =
        when (forceDarkMode) {
            ForceDarkMode.OFF -> false
            ForceDarkMode.ON -> true
            ForceDarkMode.FOLLOW_SYSTEM -> systemInDarkMode()
        }

internal fun setFallbackSelectors(fallback: List<String>) {
    selectors = fallback
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
        Logger.info("prefs ready count=${selectors.size}")
    }.onFailure { Logger.log(Log.ERROR, "prefs init", it) }
}

internal fun loadHookPrefs(prefs: SharedPreferences) {
    runCatching {
        val raw = Prefs.CACHED_SELECTORS.read(prefs)
        selectors = SelectorSanitizer.sanitize(raw.lineSequence())
        debugLogs = Prefs.DEBUG_LOGS.read(prefs)
        injectionEnabled = Prefs.INJECTION_ENABLED.read(prefs)
        webviewDebugging = Prefs.WEBVIEW_DEBUGGING.read(prefs)
        forceDarkMode = Prefs.readForceDarkMode(prefs)
        priceChartsEnabled = Prefs.PRICE_CHARTS_ENABLED.read(prefs)
        hideRufus = Prefs.HIDE_RUFUS.read(prefs)
    }.onFailure { Logger.log(Log.ERROR, "prefs refresh", it) }
}

private fun systemInDarkMode(): Boolean {
    val uiMode = currentApplication()?.resources?.configuration?.uiMode ?: return false
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Volatile private var cachedApplication: Application? = null

// application is stable per process so cache after the first successful lookup
private fun currentApplication(): Application? =
    cachedApplication ?: runCatching {
        Class
            .forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Application
    }.getOrNull()?.also { cachedApplication = it }
