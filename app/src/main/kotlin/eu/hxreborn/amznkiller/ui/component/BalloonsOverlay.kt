package eu.hxreborn.amznkiller.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.hxreborn.amznkiller.R
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private const val COLUMN_COUNT = 5
private const val BALLOONS_PER_COLUMN = 5
private const val AUTO_DISMISS_MS = 7_000L
private const val GRACE_MS = 1_500L

private data class BalloonSpec(
    val column: Int,
    val startDelayMs: Long,
    val durationMs: Int,
    val sizeDp: Dp,
    val swayDp: Dp,
    val swayPhase: Float,
    val rotationDeg: Float,
    val horizontalJitter: Float,
)

@Composable
fun BalloonsOverlay(onDismiss: () -> Unit) {
    var dismissible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(GRACE_MS)
        dismissible = true
    }

    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)).clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = { if (dismissible) onDismiss() },
                ),
        ) {
            BalloonField()
        }

        LaunchedEffect(Unit) {
            delay(AUTO_DISMISS_MS)
            onDismiss()
        }
    }
}

@Composable
private fun BalloonField() {
    val specs =
        remember {
            buildList {
                repeat(COLUMN_COUNT) { col ->
                    repeat(BALLOONS_PER_COLUMN) { row ->
                        add(
                            BalloonSpec(
                                column = col,
                                startDelayMs = (col * 180L) + (row * 900L) + Random.nextLong(0, 500),
                                durationMs = 4500 + Random.nextInt(2500),
                                sizeDp = (64 + Random.nextInt(48)).dp,
                                swayDp = (16 + Random.nextInt(24)).dp,
                                swayPhase = Random.nextFloat() * 2f * Math.PI.toFloat(),
                                rotationDeg = -8f + Random.nextFloat() * 16f,
                                horizontalJitter = -0.5f + Random.nextFloat(),
                            ),
                        )
                    }
                }
            }
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val columnWidth = widthPx / COLUMN_COUNT

        specs.forEach { spec ->
            Balloon(
                spec = spec,
                containerWidthPx = widthPx,
                containerHeightPx = heightPx,
                columnWidthPx = columnWidth,
            )
        }
    }
}

@Composable
private fun Balloon(
    spec: BalloonSpec,
    containerWidthPx: Float,
    containerHeightPx: Float,
    columnWidthPx: Float,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(spec) {
        delay(spec.startDelayMs)
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = spec.durationMs, easing = LinearEasing),
            )
        }
    }

    val density = LocalDensity.current
    val sizePx = with(density) { spec.sizeDp.toPx() }
    val swayPx = with(density) { spec.swayDp.toPx() }

    val p = progress.value
    val columnCenter = columnWidthPx * (spec.column + 0.5f)
    val jitter = columnWidthPx * 0.25f * spec.horizontalJitter
    val sway = sin(p * 2f * Math.PI.toFloat() + spec.swayPhase) * swayPx
    val xPx = (columnCenter + jitter + sway - sizePx / 2f).coerceIn(0f, containerWidthPx - sizePx)
    val yPx = containerHeightPx - p * (containerHeightPx + sizePx * 2f)

    val fadeIn = (p * 5f).coerceAtMost(1f)
    val fadeOut = ((1f - p) * 5f).coerceAtMost(1f)
    val alpha = (fadeIn * fadeOut).coerceIn(0f, 1f)

    Image(
        painter = painterResource(R.drawable.easter_balloon),
        contentDescription = null,
        modifier =
            Modifier
                .size(spec.sizeDp)
                .graphicsLayer {
                    translationX = xPx
                    translationY = yPx
                    this.alpha = alpha
                }.rotate(spec.rotationDeg + sin(p * 4f * Math.PI.toFloat() + spec.swayPhase) * 6f),
    )
}
