package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

private enum class StatusIconKey { Refreshing, Error, UpToDate, UpdatedDelta, Stale }

@Composable
internal fun lastCheckedLine(lastFetched: Long): String = stringResource(R.string.dashboard_last_checked, relativeTime(lastFetched))

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
    val iconKey: StatusIconKey,
    val iconTint: Color,
    val subtitleColor: Color,
)

@Composable
private fun updateStatusUi(
    status: UpdateStatus,
    errorEvent: SelectorSyncEvent.Error?,
    hasUpdatedDelta: Boolean,
    lastFetched: Long,
): UpdateStatusUi {
    val lastChecked = if (lastFetched > 0L) lastCheckedLine(lastFetched) else null
    val context = LocalContext.current
    return when (status) {
        UpdateStatus.Refreshing -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_checking_title),
                subtitle = stringResource(R.string.hero_checking_subtitle),
                iconKey = StatusIconKey.Refreshing,
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
                iconKey = StatusIconKey.Error,
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
                    ).joinToString("\n"),
                iconKey = if (hasUpdatedDelta) StatusIconKey.UpdatedDelta else StatusIconKey.UpToDate,
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
                iconKey = StatusIconKey.Stale,
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
    val updatedEvent =
        (event as? SelectorSyncEvent.Updated)
            ?.takeIf { it.added > 0 || it.removed > 0 }

    val status =
        resolveUpdateStatus(
            isRefreshing = isRefreshing,
            isRefreshFailed = isRefreshFailed,
            isStale = isStale,
            lastFetched = lastFetched,
            event = event,
        )
    val chipsVisible = status == UpdateStatus.UpToDate && updatedEvent != null
    val ui =
        updateStatusUi(
            status = status,
            errorEvent = errorEvent,
            hasUpdatedDelta = chipsVisible,
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
            StatusIcon(
                ui = ui,
                pulseKey = lastRefreshOutcome?.id?.takeIf { chipsVisible },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(ui.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = ui.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.subtitleColor,
                )
                val outcomeId = lastRefreshOutcome?.id
                if (updatedEvent != null && outcomeId != null) {
                    key(outcomeId) {
                        DeltaChips(
                            added = updatedEvent.added,
                            removed = updatedEvent.removed,
                            visible = chipsVisible,
                            modifier = Modifier.padding(top = Tokens.SpacingSm),
                        )
                    }
                }
            }
            val isErrorState = status == UpdateStatus.Error
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(
                    imageVector = if (isErrorState) Icons.Rounded.Replay else Icons.Rounded.Refresh,
                    contentDescription =
                        stringResource(
                            if (isErrorState) R.string.cd_retry else R.string.cd_refresh,
                        ),
                    tint =
                        if (isErrorState) {
                            MaterialTheme.colorScheme.error
                        } else {
                            LocalContentColor.current
                        },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusIcon(
    ui: UpdateStatusUi,
    pulseKey: Any?,
) {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(pulseKey) {
        if (pulseKey != null) {
            pulse.snapTo(0.85f)
            pulse.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
            )
        }
    }
    val fadeSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val scaleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    Box(
        modifier =
            Modifier
                .size(Tokens.IconMd)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = ui.iconKey,
            transitionSpec = {
                (
                    fadeIn(animationSpec = fadeSpec) +
                        scaleIn(animationSpec = scaleSpec, initialScale = 0.8f)
                ).togetherWith(
                    fadeOut(animationSpec = fadeSpec) +
                        scaleOut(animationSpec = scaleSpec, targetScale = 0.8f),
                )
            },
            label = "statusIcon",
        ) { key ->
            val image =
                when (key) {
                    StatusIconKey.Refreshing -> null
                    StatusIconKey.Error -> Icons.Outlined.ErrorOutline
                    StatusIconKey.UpToDate -> Icons.Rounded.CloudDone
                    StatusIconKey.UpdatedDelta -> Icons.Rounded.CloudSync
                    StatusIconKey.Stale -> Icons.Rounded.SystemUpdate
                }
            if (image == null) {
                LoadingIndicator(modifier = Modifier.size(Tokens.IconMd))
            } else {
                Icon(
                    imageVector = image,
                    contentDescription = null,
                    tint = ui.iconTint,
                    modifier = Modifier.size(Tokens.IconMd),
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
private fun UpdatesCardUpdatedBothPreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = false,
            lastFetched = System.currentTimeMillis() - 30_000,
            lastRefreshOutcome = SelectorSyncOutcome(SelectorSyncEvent.Updated(added = 2, removed = 2)),
            onRefresh = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun UpdatesCardUpdatedAddedOnlyPreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = false,
            lastFetched = System.currentTimeMillis() - 30_000,
            lastRefreshOutcome = SelectorSyncOutcome(SelectorSyncEvent.Updated(added = 5, removed = 0)),
            onRefresh = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun UpdatesCardUpdatedRemovedOnlyPreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = false,
            lastFetched = System.currentTimeMillis() - 30_000,
            lastRefreshOutcome = SelectorSyncOutcome(SelectorSyncEvent.Updated(added = 0, removed = 3)),
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

@PreviewLightDark
@Composable
private fun UpdatesCardErrorPreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = true,
            isStale = false,
            lastFetched = System.currentTimeMillis() - 7_200_000,
            lastRefreshOutcome =
                SelectorSyncOutcome(
                    SelectorSyncEvent.Error(messageResId = R.string.snackbar_update_failed),
                ),
            onRefresh = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun UpdatesCardStalePreview() {
    PreviewWrapper {
        UpdatesCard(
            isRefreshing = false,
            isRefreshFailed = false,
            isStale = true,
            lastFetched = System.currentTimeMillis() - 90_000_000,
            lastRefreshOutcome = null,
            onRefresh = {},
        )
    }
}
