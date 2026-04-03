package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.SelectorSyncEvent
import eu.hxreborn.amznkiller.ui.state.SelectorSyncOutcome
import eu.hxreborn.amznkiller.ui.state.resolveMessage
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.relativeTime

internal enum class UpdateStatus { Refreshing, Error, UpToDate, Stale }

@Composable
internal fun lastCheckedLine(lastFetched: Long): String = stringResource(R.string.dashboard_last_checked, relativeTime(lastFetched))

internal fun formatUpdateEventMessage(
    context: Context,
    event: SelectorSyncEvent,
): String =
    when (event) {
        is SelectorSyncEvent.Updated -> {
            val total = event.added + event.removed
            context.getString(R.string.snackbar_updated, total)
        }

        is SelectorSyncEvent.UpToDate -> {
            context.getString(R.string.snackbar_up_to_date)
        }

        is SelectorSyncEvent.Error -> {
            event.resolveMessage { context.getString(it) }
        }
    }

@Composable
internal fun UpdatesCard(
    isRefreshing: Boolean,
    isRefreshFailed: Boolean,
    isStale: Boolean,
    lastFetched: Long,
    lastRefreshOutcome: SelectorSyncOutcome?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val outcomeEvent = lastRefreshOutcome?.event
    val isError = outcomeEvent is SelectorSyncEvent.Error
    val isPersistedFailure = isRefreshFailed && !isError

    val status =
        when {
            isRefreshing -> UpdateStatus.Refreshing
            isError || isPersistedFailure -> UpdateStatus.Error
            !isStale && !isError && !isPersistedFailure && lastFetched > 0L -> UpdateStatus.UpToDate
            else -> UpdateStatus.Stale
        }

    val surface = MaterialTheme.colorScheme.surfaceVariant
    val shape = Tokens.CardShape
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = surface, shape = shape)
                .clip(shape)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (status) {
                UpdateStatus.Refreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }

                UpdateStatus.Error -> {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                UpdateStatus.UpToDate -> {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                UpdateStatus.Stale -> {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    when (status) {
                        UpdateStatus.Refreshing -> stringResource(R.string.hero_checking_title)
                        UpdateStatus.Error -> stringResource(R.string.hero_error_title)
                        UpdateStatus.UpToDate -> stringResource(R.string.hero_operational_title)
                        UpdateStatus.Stale -> stringResource(R.string.hero_stale_title)
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
            val lastChecked = if (lastFetched > 0L) lastCheckedLine(lastFetched) else null
            Text(
                text =
                    when (status) {
                        UpdateStatus.Refreshing -> {
                            stringResource(R.string.hero_checking_subtitle)
                        }

                        UpdateStatus.Error -> {
                            if (isError) {
                                (outcomeEvent as SelectorSyncEvent.Error).resolveMessage(context::getString)
                            } else {
                                stringResource(R.string.hero_error_subtitle)
                            }
                        }

                        UpdateStatus.UpToDate -> {
                            val delta =
                                (outcomeEvent as? SelectorSyncEvent.Updated)?.let { ev ->
                                    when {
                                        ev.added > 0 && ev.removed > 0 -> {
                                            stringResource(R.string.hero_delta_changed, ev.added, ev.removed)
                                        }

                                        ev.added > 0 -> {
                                            stringResource(R.string.hero_delta_added, ev.added)
                                        }

                                        ev.removed > 0 -> {
                                            stringResource(R.string.hero_delta_removed, ev.removed)
                                        }

                                        else -> {
                                            null
                                        }
                                    }
                                }
                            listOfNotNull(
                                stringResource(R.string.hero_operational_subtitle),
                                lastChecked,
                                delta,
                            ).joinToString("\n")
                        }

                        UpdateStatus.Stale -> {
                            listOfNotNull(
                                stringResource(R.string.hero_stale_subtitle),
                                lastChecked,
                            ).joinToString("\n")
                        }
                    },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (status == UpdateStatus.Error) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UpdatesCardUpToDatePreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = false,
            lastFetched = System.currentTimeMillis() - 3_600_000,
            lastRefreshOutcome = null,
            onRefresh = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun UpdatesCardRefreshingPreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = true,
            isRefreshFailed = false,
            isStale = false,
            lastFetched = 0L,
            lastRefreshOutcome = null,
            onRefresh = {},
        )
    }
}
