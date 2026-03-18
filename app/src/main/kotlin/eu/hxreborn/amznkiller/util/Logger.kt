package eu.hxreborn.amznkiller.util

import android.util.Log
import eu.hxreborn.amznkiller.prefs.PrefsManager
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
        if (!PrefsManager.debugLogs) return
        log(msg)
    }

    fun logDebug(
        msg: String,
        t: Throwable,
    ) {
        if (!PrefsManager.debugLogs) return
        log(msg, t)
    }
}
