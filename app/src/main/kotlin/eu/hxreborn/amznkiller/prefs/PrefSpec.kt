package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences

class PrefSpec<T>(
    val key: String,
    val default: T,
    private val get: SharedPreferences.(String, T) -> T,
    private val put: SharedPreferences.Editor.(String, T) -> Unit,
) {
    fun read(prefs: SharedPreferences): T = prefs.get(key, default)

    fun write(
        editor: SharedPreferences.Editor,
        value: T,
    ) {
        editor.put(key, value)
    }

    fun copyIfChanged(
        from: SharedPreferences,
        to: SharedPreferences,
        editor: SharedPreferences.Editor,
    ): Boolean {
        val value = read(from)
        if (read(to) == value) return false
        write(editor, value)
        return true
    }
}

fun boolPref(
    key: String,
    default: Boolean,
) = PrefSpec(key, default, SharedPreferences::getBoolean) { k, v -> putBoolean(k, v) }

fun longPref(
    key: String,
    default: Long,
) = PrefSpec(key, default, SharedPreferences::getLong) { k, v -> putLong(k, v) }

fun stringPref(
    key: String,
    default: String,
) = PrefSpec(key, default, { k, d -> getString(k, d) ?: d }) { k, v -> putString(k, v) }
