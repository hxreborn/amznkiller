package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    val containerColor: Color,
    val accentColor: Color,
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
    val colors = MaterialTheme.colorScheme
    return when (status) {
        UpdateStatus.Refreshing -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_checking_title),
                subtitle = stringResource(R.string.hero_checking_subtitle),
                iconKey = StatusIconKey.Refreshing,
                containerColor = colors.secondaryContainer,
                accentColor = colors.secondary,
            )
        }

        UpdateStatus.Error -> {
            UpdateStatusUi(
                title = stringResource(R.string.hero_error_title),
                subtitle =
                    errorEvent?.resolveMessage(context::getString)
                        ?: stringResource(R.string.hero_error_subtitle),
                iconKey = StatusIconKey.Error,
                containerColor = colors.errorContainer,
                accentColor = colors.error,
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
                containerColor = colors.primaryContainer,
                accentColor = colors.primary,
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
                containerColor = colors.tertiaryContainer,
                accentColor = colors.tertiary,
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
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val containerColor by animateColorAsState(ui.containerColor, colorSpec)
    val contentColor by animateColorAsState(contentColorFor(ui.containerColor), colorSpec)

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Tokens.SpacingSm),
        shape = Tokens.CardShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(Tokens.CardInnerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingLg),
        ) {
            StatusBadge(
                ui = ui,
                pulseKey = lastRefreshOutcome?.id?.takeIf { chipsVisible },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(ui.title, style = MaterialTheme.typography.titleMediumEmphasized)
                Text(ui.subtitle, style = MaterialTheme.typography.bodyMedium)
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
            FilledIconButton(
                onClick = onRefresh,
                shapes = IconButtonDefaults.shapes(),
                enabled = !isRefreshing,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ui.accentColor),
            ) {
                Icon(
                    imageVector = if (isErrorState) Icons.Rounded.Replay else Icons.Rounded.Refresh,
                    contentDescription =
                        stringResource(
                            if (isErrorState) R.string.cd_retry else R.string.cd_refresh,
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusBadge(
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
    AnimatedContent(
        targetState = ui,
        modifier =
            Modifier
                .size(LoadingIndicatorDefaults.ContainerWidth)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                },
        transitionSpec = {
            (
                fadeIn(animationSpec = fadeSpec) +
                    scaleIn(animationSpec = scaleSpec, initialScale = 0.8f)
            ).togetherWith(
                fadeOut(animationSpec = fadeSpec) +
                    scaleOut(animationSpec = scaleSpec, targetScale = 0.8f),
            )
        },
        contentKey = { it.iconKey },
        label = "statusBadge",
    ) { target ->
        val image =
            when (target.iconKey) {
                StatusIconKey.Refreshing -> null
                StatusIconKey.Error -> Icons.Outlined.ErrorOutline
                StatusIconKey.UpToDate -> Icons.Rounded.CloudDone
                StatusIconKey.UpdatedDelta -> Icons.Rounded.CloudSync
                StatusIconKey.Stale -> Icons.Rounded.SystemUpdate
            }
        if (image == null) {
            ContainedLoadingIndicator(
                containerColor = target.accentColor,
                indicatorColor = contentColorFor(target.accentColor),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(target.accentColor, MaterialShapes.Cookie9Sided.toShape()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = null,
                    tint = contentColorFor(target.accentColor),
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
