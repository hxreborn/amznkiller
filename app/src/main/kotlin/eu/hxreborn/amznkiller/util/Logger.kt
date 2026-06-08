package eu.hxreborn.amznkiller.util

import android.util.Log
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.xposed.AmznkillerModule.Companion.TAG
import eu.hxreborn.amznkiller.xposed.hook.cachedDebugLogs
import eu.hxreborn.amznkiller.xposed.module

object Logger {
    fun log(
        level: Int,
        msg: String,
        t: Throwable? = null,
    ) = if (t != null) module.log(level, TAG, msg, t) else module.log(level, TAG, msg)

    fun info(msg: String) = module.log(Log.INFO, TAG, msg)

    inline fun debug(msg: () -> String) {
        if (debugEnabled()) module.log(Log.DEBUG, TAG, msg())
    }

    @PublishedApi
    internal fun debugEnabled(): Boolean = BuildConfig.DEBUG || cachedDebugLogs
}
