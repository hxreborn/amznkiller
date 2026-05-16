package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
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
            context.resources.getQuantityString(R.plurals.snackbar_updated, total, total)
        }

        is SelectorSyncEvent.UpToDate -> {
            context.getString(R.string.snackbar_up_to_date)
        }

        is SelectorSyncEvent.Error -> {
            event.resolveMessage(context::getString)
        }
    }

private fun resolveUpdateStatus(
    isRefreshing: Boolean,
    isRefreshFailed: Boolean,
    isStale: Boolean,
    lastFetched: Long,
    event: SelectorSyncEvent?,
): UpdateStatus =
    when {
        isRefreshing -> UpdateStatus.Refreshing
        event is SelectorSyncEvent.Error -> UpdateStatus.Error
        isRefreshFailed -> UpdateStatus.Error
        !isStale && lastFetched > 0L -> UpdateStatus.UpToDate
        else -> UpdateStatus.Stale
    }

private data class UpdateStatusUi(
    val title: String,
    val subtitle: String,
    val iconImage: ImageVector?,
    val iconTint: Color,
    val subtitleColor: Color,
)

@Composable
private fun deltaLineOrNull(event: SelectorSyncEvent.Updated): String? =
    when {
        event.added > 0 && event.removed > 0 -> {
            stringResource(R.string.hero_delta_changed, event.added, event.removed)
        }

        event.added > 0 -> {
            pluralStringResource(R.plurals.hero_delta_added, event.added, event.added)
        }

        event.removed > 0 -> {
            pluralStringResource(R.plurals.hero_delta_removed, event.removed, event.removed)
        }

        else -> {
            null
        }
    }

@Composable
private fun updateStatusUi(
    status: UpdateStatus,
    errorEvent: SelectorSyncEvent.Error?,
    deltaLine: String?,
    lastFetched: Long,
): UpdateStatusUi {
    val lastChecked = if (lastFetched > 0L) lastCheckedLine(lastFetched) else null
    val context = LocalContext.current
    return when (status) {
        UpdateStatus.Refreshing -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_checking_title),
                subtitle = stringResource(R.string.hero_checking_subtitle),
                iconImage = null,
                iconTint = MaterialTheme.colorScheme.primary,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        UpdateStatus.Error -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_error_title),
                subtitle =
                    errorEvent?.resolveMessage(context::getString)
                        ?: stringResource(R.string.hero_error_subtitle),
                iconImage = Icons.Outlined.ErrorOutline,
                iconTint = MaterialTheme.colorScheme.error,
                subtitleColor = MaterialTheme.colorScheme.error,
            )
        }

        UpdateStatus.UpToDate -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_operational_title),
                subtitle =
                    listOfNotNull(
                        stringResource(R.string.hero_operational_subtitle),
                        lastChecked,
                        deltaLine,
                    ).joinToString("\n"),
                iconImage = Icons.Rounded.CloudDone,
                iconTint = MaterialTheme.colorScheme.primary,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        UpdateStatus.Stale -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_stale_title),
                subtitle =
                    listOfNotNull(
                        stringResource(R.string.hero_stale_subtitle),
                        lastChecked,
                    ).joinToString("\n"),
                iconImage = Icons.Rounded.SystemUpdate,
                iconTint = MaterialTheme.colorScheme.tertiary,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val event = lastRefreshOutcome?.event
    val errorEvent = event as? SelectorSyncEvent.Error
    val updatedEvent = event as? SelectorSyncEvent.Updated

    val status =
        resolveUpdateStatus(
            isRefreshing = isRefreshing,
            isRefreshFailed = isRefreshFailed,
            isStale = isStale,
            lastFetched = lastFetched,
            event = event,
        )
    val deltaLine = updatedEvent?.let { deltaLineOrNull(it) }
    val ui =
        updateStatusUi(
            status = status,
            errorEvent = errorEvent,
            deltaLine = deltaLine,
            lastFetched = lastFetched,
        )

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Tokens.SpacingSm),
        shape = Tokens.CardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(Tokens.CardInnerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingLg),
        ) {
            Box(
                modifier = Modifier.size(Tokens.IconMd),
                contentAlignment = Alignment.Center,
            ) {
                val image = ui.iconImage
                if (image == null) {
                    LoadingIndicator(modifier = Modifier.size(Tokens.IconMd))
                } else {
                    Icon(
                        imageVector = image,
                        contentDescription = null,
                        tint = ui.iconTint,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(ui.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = ui.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.subtitleColor,
                )
            }
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
