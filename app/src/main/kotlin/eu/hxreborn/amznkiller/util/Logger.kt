package eu.hxreborn.amznkiller.util

import android.content.SharedPreferences
import android.util.Log
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.Prefs
import io.github.libxposed.api.XposedModule

object Logger {
    private const val TAG = "AmznKiller"

    private var module: XposedModule? = null

    fun init(module: XposedModule) {
        this.module = module
    }

    fun log(msg: String) {
        module?.log(Log.INFO, TAG, msg)
        Log.d(TAG, msg)
    }

    fun log(
        msg: String,
        t: Throwable,
    ) {
        module?.log(Log.ERROR, TAG, msg, t)
        Log.d(TAG, msg, t)
    }

    fun logDebug(msg: String) {
        if (!debugEnabled()) return
        log(msg)
    }

    fun logDebug(
        msg: String,
        t: Throwable,
    ) {
        if (!debugEnabled()) return
        log(msg, t)
    }

    // LSPosed remote prefs do not fire change listeners — poll on each call
    private fun debugEnabled(): Boolean =
        BuildConfig.DEBUG || cachedPrefs()?.let { Prefs.DEBUG_LOGS.read(it) } == true

    @Volatile
    private var prefs: SharedPreferences? = null

    private fun cachedPrefs(): SharedPreferences? =
        prefs ?: runCatching { module?.getRemotePreferences(Prefs.GROUP) }
            .getOrNull()
            ?.also { prefs = it }
}
