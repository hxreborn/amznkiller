package eu.hxreborn.amznkiller.ui.util

import java.text.DateFormat
import java.util.Date

internal fun relativeTime(millis: Long): String {
    if (millis == 0L) return "never"
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000 -> {
            "just now"
        }

        diff < 3_600_000 -> {
            "${diff / 60_000}m ago"
        }

        diff < 86_400_000 -> {
            "${diff / 3_600_000}h ago"
        }

        diff < 172_800_000 -> {
            "yesterday"
        }

        else -> {
            DateFormat.getDateInstance(DateFormat.SHORT).format(Date(millis))
        }
    }
}
