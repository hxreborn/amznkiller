package eu.hxreborn.amznkiller.util

import android.util.Log

object Logger {
    private const val TAG = "AmznKiller"

    private var module: io.github.libxposed.api.XposedModule? = null

    fun init(module: io.github.libxposed.api.XposedModule) {
        this.module = module
    }

    fun log(
        msg: String,
        t: Throwable? = null,
    ) {
        if (t == null) {
            module?.log(msg)
            Log.d(TAG, msg)
        } else {
            module?.log(msg, t)
            Log.d(TAG, msg, t)
        }
    }
}
