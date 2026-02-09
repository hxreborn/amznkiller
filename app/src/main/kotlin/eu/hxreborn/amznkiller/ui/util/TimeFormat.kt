package eu.hxreborn.amznkiller.ui.util

import java.text.DateFormat
import java.util.Date

internal fun relativeTime(millis: Long): String {
    if (millis <= 0L) return "never"

    val now = System.currentTimeMillis()
    val diff = now - millis
    diff / 1000

    return when {
        diff < 5_000 -> "just now"
        diff < 15_000 -> "< 15s ago"
        diff < 30_000 -> "< 30s ago"
        diff < 60_000 -> "< 1m ago"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> DateFormat.getDateInstance(DateFormat.SHORT).format(Date(millis))
    }
}
