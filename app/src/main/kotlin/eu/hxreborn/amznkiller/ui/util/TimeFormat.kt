package eu.hxreborn.amznkiller.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun relativeTime(millis: Long): String {
    if (millis <= 0L) return "never"

    val now = System.currentTimeMillis()
    val diff = now - millis

    return when {
        diff < 5_000 -> {
            "just now"
        }

        diff < 15_000 -> {
            "< 15s ago"
        }

        diff < 30_000 -> {
            "< 30s ago"
        }

        diff < 60_000 -> {
            "< 1m ago"
        }

        diff < 3_600_000 -> {
            "${diff / 60_000}m ago"
        }

        diff < 86_400_000 -> {
            "${diff / 3_600_000}h ago"
        }

        diff < 604_800_000 -> {
            "${diff / 86_400_000}d ago"
        }

        else -> {
            val instant = Instant.ofEpochMilli(millis)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(localDate)
        }
    }
}
