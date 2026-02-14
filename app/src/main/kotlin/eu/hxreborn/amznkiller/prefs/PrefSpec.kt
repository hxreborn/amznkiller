package eu.hxreborn.amznkiller.prefs

import android.content.SharedPreferences

sealed class PrefSpec<T : Any>(
    val key: String,
    val default: T,
) {
    abstract fun read(prefs: SharedPreferences): T

    abstract fun write(
        editor: SharedPreferences.Editor,
        value: T,
    )

    fun reset(editor: SharedPreferences.Editor) = write(editor, default)

    fun copyTo(
        from: SharedPreferences,
        to: SharedPreferences.Editor,
    ) = write(to, read(from))
}

class BoolPref(
    key: String,
    default: Boolean,
) : PrefSpec<Boolean>(key, default) {
    override fun read(prefs: SharedPreferences): Boolean = prefs.getBoolean(key, default)

    override fun write(
        editor: SharedPreferences.Editor,
        value: Boolean,
    ) {
        editor.putBoolean(key, value)
    }
}

class StringPref(
    key: String,
    default: String,
) : PrefSpec<String>(key, default) {
    override fun read(prefs: SharedPreferences): String = prefs.getString(key, default) ?: default

    override fun write(
        editor: SharedPreferences.Editor,
        value: String,
    ) {
        editor.putString(key, value)
    }
}

class LongPref(
    key: String,
    default: Long,
) : PrefSpec<Long>(key, default) {
    override fun read(prefs: SharedPreferences): Long = prefs.getLong(key, default)

    override fun write(
        editor: SharedPreferences.Editor,
        value: Long,
    ) {
        editor.putLong(key, value)
    }
}
