package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ExtensionOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.graphics.shapes.Morph
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.component.fitted
import eu.hxreborn.amznkiller.ui.component.rotationSafeScale
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SelectorSyncEvent
import eu.hxreborn.amznkiller.ui.state.SelectorSyncOutcome
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.relativeTime
import eu.hxreborn.amznkiller.ui.util.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.roundToInt

internal enum class DashboardStatus(
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val action: Int?,
    val icon: ImageVector?,
) {
    ModuleInactive(R.string.status_inactive_title, R.string.status_inactive_body, null, Icons.Rounded.ExtensionOff),
    AmazonMissing(
        R.string.status_no_amazon_title,
        R.string.status_no_amazon_body,
        R.string.status_no_amazon_action,
        Icons.Rounded.RemoveShoppingCart,
    ),
    Paused(R.string.status_paused_title, R.string.status_paused_body, null, Icons.Rounded.Pause),
    Refreshing(R.string.status_refreshing_title, R.string.status_refreshing_body, null, null),
    NoRules(R.string.status_no_rules_title, R.string.status_no_rules_body, R.string.status_no_rules_action, Icons.Rounded.CloudDownload),
    SyncFailed(R.string.status_failed_title, R.string.status_failed_body, R.string.status_failed_action, Icons.Rounded.CloudOff),
    Stale(R.string.status_stale_title, R.string.status_stale_body, R.string.status_stale_action, Icons.Rounded.Update),
    Blocking(R.string.status_blocking_title, R.string.status_blocking_body, R.string.status_blocking_action, Icons.Rounded.Check),
}

internal fun resolveDashboardStatus(
    state: DashboardUiState.Ready,
    isAmazonInstalled: Boolean,
): DashboardStatus =
    when {
        !state.isXposedActive -> DashboardStatus.ModuleInactive
        !isAmazonInstalled -> DashboardStatus.AmazonMissing
        !state.injectionEnabled -> DashboardStatus.Paused
        state.isRefreshing -> DashboardStatus.Refreshing
        state.selectorCount == 0 -> DashboardStatus.NoRules
        state.isRefreshFailed -> DashboardStatus.SyncFailed
        state.isStale -> DashboardStatus.Stale
        else -> DashboardStatus.Blocking
    }

@Composable
private fun statusBody(
    status: DashboardStatus,
    selectorCount: Int,
    lastFetched: Long,
): String =
    when (status) {
        DashboardStatus.Blocking -> {
            val rules = pluralStringResource(R.plurals.dashboard_rules_count, selectorCount, selectorCount)
            stringResource(status.body, rules, relativeTime(lastFetched))
        }

        DashboardStatus.Stale -> {
            stringResource(status.body, relativeTime(lastFetched))
        }

        else -> {
            stringResource(status.body)
        }
    }

@Composable
private fun DashboardStatus.colors(): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        DashboardStatus.Blocking -> {
            colors.primaryContainer to colors.primary
        }

        DashboardStatus.Refreshing -> {
            colors.secondaryContainer to colors.secondary
        }

        DashboardStatus.Paused, DashboardStatus.Stale -> {
            colors.tertiaryContainer to colors.tertiary
        }

        DashboardStatus.ModuleInactive, DashboardStatus.AmazonMissing, DashboardStatus.NoRules, DashboardStatus.SyncFailed -> {
            colors.errorContainer to colors.error
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun StatusCard(
    status: DashboardStatus,
    selectorCount: Int,
    lastFetched: Long,
    lastRefreshOutcome: SelectorSyncOutcome?,
    pullProgress: () -> Float,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = status, label = "status")
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val containerColor by transition.animateColor(transitionSpec = { colorSpec }, label = "container") { it.colors().first }
    val contentColor by transition.animateColor(transitionSpec = { colorSpec }, label = "content") { contentColorFor(it.colors().first) }
    val accent by transition.animateColor(transitionSpec = { colorSpec }, label = "accent") { it.colors().second }
    val onAccent by transition.animateColor(transitionSpec = { colorSpec }, label = "onAccent") { contentColorFor(it.colors().second) }
    val fadeSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    val opensAmazon = status == DashboardStatus.Blocking
    Surface(
        onClick = onAction,
        enabled = opensAmazon,
        modifier = modifier.fillMaxWidth().padding(horizontal = Tokens.ScreenHorizontalPadding),
        shape = Tokens.CardShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(Tokens.CardInnerPadding),
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(status = status, accent = accent, onAccent = onAccent, pullProgress = pullProgress)
            transition.AnimatedContent(
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    fadeIn(fadeSpec).togetherWith(fadeOut(fadeSpec)).using(SizeTransform(clip = false) { _, _ -> sizeSpec })
                },
            ) { target ->
                Column(verticalArrangement = Arrangement.spacedBy(Tokens.SpacingXs)) {
                    Text(stringResource(target.title), style = MaterialTheme.typography.titleLargeEmphasized)
                    Text(statusBody(target, selectorCount, lastFetched), style = MaterialTheme.typography.bodyLarge)
                    val updated = (lastRefreshOutcome?.event as? SelectorSyncEvent.Updated)?.takeIf { it.added > 0 || it.removed > 0 }
                    if (updated != null && lastRefreshOutcome != null) {
                        key(lastRefreshOutcome.id) {
                            DeltaChips(
                                added = updated.added,
                                removed = updated.removed,
                                visible = target == DashboardStatus.Blocking,
                                modifier = Modifier.padding(top = Tokens.SpacingXs),
                            )
                        }
                    }
                    if (target != DashboardStatus.Blocking) {
                        target.action?.let { action ->
                            Button(
                                onClick = onAction,
                                shapes = ButtonDefaults.shapes(),
                                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                                modifier = Modifier.padding(top = Tokens.SpacingSm).align(Alignment.End),
                            ) {
                                Text(stringResource(action))
                            }
                        }
                    }
                }
            }
            if (opensAmazon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(DashboardStatus.Blocking.action!!),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val BadgePolygons =
    listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Pentagon,
        MaterialShapes.Pill,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.SoftBurst,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val DashboardStatus.restIndex: Int
    get() = if (this == DashboardStatus.Blocking) BadgePolygons.indexOf(MaterialShapes.Sunny) else 0
private const val MORPH_INTERVAL_MILLIS = 650L
private const val STEP_TURN_DEGREES = 60f
private const val PULL_TURN_DEGREES = 90f
private const val ICON_ENTER_SCALE = 0.92f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusBadge(
    status: DashboardStatus,
    accent: Color,
    onAccent: Color,
    pullProgress: () -> Float,
) {
    val morphs =
        remember {
            BadgePolygons.indices.map {
                Morph(
                    BadgePolygons[it].normalized(),
                    BadgePolygons[(it + 1) % BadgePolygons.size].normalized(),
                )
            }
        }
    val scaleFactor = remember { rotationSafeScale(BadgePolygons) }
    val refreshing = status == DashboardStatus.Refreshing
    val reducedMotion = rememberReducedMotion()
    var step by remember { mutableIntStateOf(status.restIndex) }
    LaunchedEffect(status, reducedMotion) {
        if (refreshing && !reducedMotion) {
            while (true) {
                delay(MORPH_INTERVAL_MILLIS)
                step += 1
            }
        } else {
            val rest = status.restIndex
            step = ((step - rest).toFloat() / morphs.size).roundToInt() * morphs.size + rest
        }
    }
    val morphSpec = remember { spring<Float>(dampingRatio = 0.6f, stiffness = 200f) }
    val settleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val transition = updateTransition(targetState = step, label = "badgeStep")
    val progress by transition.animateFloat(
        transitionSpec = { if (refreshing) morphSpec else settleSpec },
        label = "badgeMorph",
    ) { it.toFloat() }

    val path = remember { Path() }
    val scaleMatrix = remember { Matrix() }
    Box(
        modifier =
            Modifier.size(Tokens.StatusBadgeSize).drawBehind {
                val whole = floor(progress)
                val index = Math.floorMod(whole.toInt(), morphs.size)
                rotate(progress * STEP_TURN_DEGREES + pullProgress() * PULL_TURN_DEGREES) {
                    drawPath(fitted(morphs[index].toPath(progress - whole, path), size, scaleFactor, scaleMatrix), accent)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val fadeSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        val scaleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
        AnimatedContent(
            targetState = status.icon,
            transitionSpec = {
                (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = ICON_ENTER_SCALE))
                    .togetherWith(fadeOut(fadeSpec) + scaleOut(scaleSpec, targetScale = ICON_ENTER_SCALE))
            },
            label = "statusIcon",
        ) { icon ->
            if (icon != null) Icon(imageVector = icon, contentDescription = null, tint = onAccent)
        }
    }
}

@PreviewLightDark
@Composable
private fun StatusCardBlockingPreview() {
    PreviewWrapper {
        StatusCard(
            status = DashboardStatus.Blocking,
            selectorCount = 90,
            lastFetched = System.currentTimeMillis() - 7_200_000,
            lastRefreshOutcome = SelectorSyncOutcome(SelectorSyncEvent.Updated(added = 12, removed = 3)),
            pullProgress = { 0f },
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun StatusCardStalePreview() {
    PreviewWrapper {
        StatusCard(
            status = DashboardStatus.Stale,
            selectorCount = 90,
            lastFetched = System.currentTimeMillis() - 259_200_000,
            lastRefreshOutcome = null,
            pullProgress = { 0f },
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun StatusCardInactivePreview() {
    PreviewWrapper {
        StatusCard(
            status = DashboardStatus.ModuleInactive,
            selectorCount = 0,
            lastFetched = 0L,
            lastRefreshOutcome = null,
            pullProgress = { 0f },
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun StatusCardRefreshingPreview() {
    PreviewWrapper {
        StatusCard(
            status = DashboardStatus.Refreshing,
            selectorCount = 90,
            lastFetched = 0L,
            lastRefreshOutcome = null,
            pullProgress = { 0f },
            onAction = {},
        )
    }
}
