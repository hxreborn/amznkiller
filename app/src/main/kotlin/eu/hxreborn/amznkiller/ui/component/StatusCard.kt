package eu.hxreborn.amznkiller.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.animation.FillLevelState
import eu.hxreborn.amznkiller.ui.animation.rememberWavePhase
import eu.hxreborn.amznkiller.ui.animation.waveFill

@Composable
fun StatusCard(
    isActive: Boolean,
    fillState: FillLevelState,
    selectorCount: Int,
    lastFetched: Long,
    onShowRules: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint =
        if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        }

    val wavePhase = rememberWavePhase()
    val showLiquid = fillState.value > 0f

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector =
                        if (showLiquid || !isActive) {
                            Icons.Outlined.Shield
                        } else {
                            Icons.Rounded.Shield
                        },
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = iconTint,
                )

                if (showLiquid) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(80.dp)
                                .waveFill(fillState.value, wavePhase),
                        tint = iconTint,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text =
                    if (isActive) {
                        stringResource(R.string.status_active)
                    } else {
                        stringResource(R.string.status_inactive)
                    },
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(Modifier.height(4.dp))

            if (selectorCount == 0) {
                Text(
                    text = stringResource(R.string.status_no_rules),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text =
                        buildAnnotatedString {
                            append("$selectorCount rules")
                            append(" \u00b7 Updated ${relativeTime(lastFetched)}")
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onShowRules),
                )
            }
        }
    }
}

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
            java.text.DateFormat
                .getDateInstance(java.text.DateFormat.SHORT)
                .format(java.util.Date(millis))
        }
    }
}
