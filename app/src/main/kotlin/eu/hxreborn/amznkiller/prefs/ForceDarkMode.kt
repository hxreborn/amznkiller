package eu.hxreborn.amznkiller.prefs

enum class ForceDarkMode {
    OFF,
    FOLLOW_SYSTEM,
    ON,
    ;

    val prefValue: String
        get() = name.lowercase()

    companion object {
        fun fromPrefValue(value: String): ForceDarkMode =
            entries.firstOrNull { it.prefValue == value.lowercase() }
                ?: when (value.lowercase()) {
                    "system",
                    "follow_system",
                    -> FOLLOW_SYSTEM

                    "true",
                    "enabled",
                    "always",
                    "dark",
                    -> ON

                    else -> OFF
                }
    }
}
