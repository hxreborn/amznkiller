package eu.hxreborn.amznkiller.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.hxreborn.amznkiller.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun relativeTime(millis: Long): String {
    if (millis <= 0L) return stringResource(R.string.time_never)

    val now = System.currentTimeMillis()
    val diff = now - millis

    return when {
        diff < 5_000 -> {
            stringResource(R.string.time_just_now)
        }

        diff < 15_000 -> {
            stringResource(R.string.time_under_15s)
        }

        diff < 30_000 -> {
            stringResource(R.string.time_under_30s)
        }

        diff < 60_000 -> {
            stringResource(R.string.time_under_1m)
        }

        diff < 3_600_000 -> {
            stringResource(R.string.time_minutes_ago, diff / 60_000)
        }

        diff < 86_400_000 -> {
            stringResource(R.string.time_hours_ago, diff / 3_600_000)
        }

        diff < 604_800_000 -> {
            stringResource(R.string.time_days_ago, diff / 86_400_000)
        }

        else -> {
            val instant = Instant.ofEpochMilli(millis)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(localDate)
        }
    }
}
