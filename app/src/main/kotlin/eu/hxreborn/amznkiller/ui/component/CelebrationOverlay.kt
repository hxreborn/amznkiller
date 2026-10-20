package eu.hxreborn.amznkiller.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.util.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_COUNT = 160
private const val FIRST_TIME_PARTICLE_COUNT = 360
private const val SECOND_WAVE_SECONDS = 0.45f
private const val BURST_SECONDS = 2.8f
private const val FADE_FROM_SECONDS = 2.0f
private const val BALLOONS_FROM_SECONDS = 0.9f
private const val TOTAL_SECONDS = 7f
private const val STATIC_SECONDS = 3f
private const val STATIC_SNAPSHOT_SECONDS = 1.1f
private const val GRACE_SECONDS = 0.6f
private const val GRAVITY_DP = 520f
private const val DRAG = 2.2f
private const val MIN_SPEED_DP = 260f
private const val MAX_SPEED_DP = 620f
private const val SPREAD_DEGREES = 75f
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f
private const val KIND_RECT = 0
private const val KIND_DOT = 1
private const val KIND_RIBBON = 2
private const val COLUMN_COUNT = 5
private const val BALLOONS_PER_COLUMN = 5
private val AmazonOrange = Color(0xFFFF9900)
private val Gold = Color(0xFFFFC107)

class ConfettiField(
    seed: Int,
    density: Float,
    private val colors: LongArray,
    firstTime: Boolean,
) {
    private val count = if (firstTime) FIRST_TIME_PARTICLE_COUNT else PARTICLE_COUNT
    private val velocityX = FloatArray(count)
    private val velocityY = FloatArray(count)
    private val width = FloatArray(count)
    private val height = FloatArray(count)
    private val spin = FloatArray(count)
    private val phase = FloatArray(count)
    private val flutterRate = FloatArray(count)
    private val delay = FloatArray(count)
    private val kind = IntArray(count)
    private val colorIndex = IntArray(count)
    private val gravity = GRAVITY_DP * density
    private val quads = Array(colors.size) { Path() }

    init {
        val random = Random(seed)
        for (i in 0 until count) {
            delay[i] = if (firstTime && i >= PARTICLE_COUNT) SECOND_WAVE_SECONDS else 0f
            val angle = (-90f + (random.nextFloat() * 2f - 1f) * SPREAD_DEGREES) * (PI.toFloat() / 180f)
            val speed = (MIN_SPEED_DP + random.nextFloat() * (MAX_SPEED_DP - MIN_SPEED_DP)) * density
            velocityX[i] = cos(angle) * speed
            velocityY[i] = sin(angle) * speed
            kind[i] =
                when {
                    i % 7 == 0 -> KIND_RIBBON
                    i % 3 == 0 -> KIND_DOT
                    else -> KIND_RECT
                }
            val scale = 0.8f + random.nextFloat() * 0.6f
            when (kind[i]) {
                KIND_RIBBON -> {
                    width[i] = 3f * density * scale
                    height[i] = 14f * density * scale
                }

                KIND_DOT -> {
                    width[i] = 3.5f * density * scale
                    height[i] = width[i]
                }

                else -> {
                    width[i] = 8f * density * scale
                    height[i] = 5f * density * scale
                }
            }
            spin[i] = (random.nextFloat() * 2f - 1f) * 720f
            phase[i] = random.nextFloat() * 360f
            flutterRate[i] = 4f + random.nextFloat() * 8f
            colorIndex[i] = random.nextInt(colors.size)
        }
    }

    fun draw(
        scope: DrawScope,
        originX: Float,
        originY: Float,
        seconds: Float,
        alpha: Float,
    ) {
        if (alpha <= 0f) return
        for (path in quads) path.rewind()
        for (i in 0 until count) {
            val t = seconds - delay[i]
            if (t < 0f) continue
            val decay = 1f - exp(-DRAG * t)
            val x = originX + velocityX[i] / DRAG * decay
            val y = originY + gravity / DRAG * t + (velocityY[i] - gravity / DRAG) / DRAG * decay
            if (kind[i] == KIND_DOT) {
                scope.drawCircle(Color(colors[colorIndex[i]].toULong()), radius = width[i], center = Offset(x, y), alpha = alpha)
                continue
            }
            val angle = (phase[i] + spin[i] * t) * DEGREES_TO_RADIANS
            val halfWidth = width[i] / 2f * cos(phase[i] * DEGREES_TO_RADIANS + flutterRate[i] * t)
            val halfHeight = height[i] / 2f
            val ux = cos(angle) * halfWidth
            val uy = sin(angle) * halfWidth
            val vx = -sin(angle) * halfHeight
            val vy = cos(angle) * halfHeight
            val path = quads[colorIndex[i]]
            path.moveTo(x + ux + vx, y + uy + vy)
            path.lineTo(x - ux + vx, y - uy + vy)
            path.lineTo(x - ux - vx, y - uy - vy)
            path.lineTo(x + ux - vx, y + uy - vy)
            path.close()
        }
        for (c in colors.indices) {
            scope.drawPath(quads[c], Color(colors[c].toULong()), alpha = alpha)
        }
    }
}

@Composable
fun CelebrationOverlay(
    originInRoot: Offset,
    startedAtNanos: Long,
    seed: Int,
    firstTime: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = rememberReducedMotion()
    val density = LocalDensity.current.density
    val scheme = MaterialTheme.colorScheme
    val burstSeconds = if (firstTime) BURST_SECONDS + SECOND_WAVE_SECONDS else BURST_SECONDS
    val field =
        remember(seed, density, scheme, firstTime) {
            mutableStateOf<ConfettiField?>(
                ConfettiField(
                    seed = seed,
                    density = density,
                    colors =
                        longArrayOf(
                            scheme.primary.value.toLong(),
                            scheme.tertiary.value.toLong(),
                            scheme.secondary.value.toLong(),
                            scheme.inversePrimary.value.toLong(),
                            AmazonOrange.value.toLong(),
                            Gold.value.toLong(),
                        ),
                    firstTime = firstTime,
                ),
            )
        }
    val seconds = remember(startedAtNanos) { mutableFloatStateOf((System.nanoTime() - startedAtNanos) / NANOS_PER_SECOND) }
    LaunchedEffect(startedAtNanos) {
        if (reducedMotion) {
            val elapsed = (System.nanoTime() - startedAtNanos) / NANOS_PER_SECOND
            delay(((STATIC_SECONDS - elapsed) * 1000f).toLong().coerceAtLeast(0L))
            onDismiss()
            return@LaunchedEffect
        }
        while (true) {
            withFrameNanos { now -> seconds.floatValue = (now - startedAtNanos) / NANOS_PER_SECOND }
            if (seconds.floatValue >= burstSeconds) field.value = null
            if (seconds.floatValue >= TOTAL_SECONDS) {
                onDismiss()
                break
            }
        }
    }
    BackHandler(onBack = onDismiss)

    var overlayInRoot by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer()
                .onGloballyPositioned { overlayInRoot = it.positionInRoot() }
                .background(Color.Black.copy(alpha = 0.18f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        if ((System.nanoTime() - startedAtNanos) / NANOS_PER_SECOND >= GRACE_SECONDS) onDismiss()
                    }
                },
    ) {
        if (!reducedMotion) BalloonField(seconds, seed)
        Canvas(Modifier.fillMaxSize()) {
            val confetti = field.value ?: return@Canvas
            val origin = originInRoot - overlayInRoot
            if (reducedMotion) {
                confetti.draw(this, origin.x, origin.y, STATIC_SNAPSHOT_SECONDS, 1f)
            } else {
                val t = seconds.floatValue.coerceAtLeast(0f)
                val fadeFrom = burstSeconds - (BURST_SECONDS - FADE_FROM_SECONDS)
                val alpha = 1f - ((t - fadeFrom) / (BURST_SECONDS - FADE_FROM_SECONDS)).coerceIn(0f, 1f)
                confetti.draw(this, origin.x, origin.y, t, alpha)
            }
        }
    }
}

private data class BalloonSpec(
    val column: Int,
    val startSeconds: Float,
    val durationSeconds: Float,
    val sizeDp: Dp,
    val swayDp: Dp,
    val swayPhase: Float,
    val rotationDeg: Float,
    val horizontalJitter: Float,
)

@Composable
private fun BalloonField(
    seconds: FloatState,
    seed: Int,
) {
    val specs =
        remember(seed) {
            val random = Random(seed + 1)
            buildList {
                repeat(COLUMN_COUNT) { col ->
                    repeat(BALLOONS_PER_COLUMN) { row ->
                        add(
                            BalloonSpec(
                                column = col,
                                startSeconds = BALLOONS_FROM_SECONDS + col * 0.18f + row * 0.9f + random.nextFloat() * 0.5f,
                                durationSeconds = 4.5f + random.nextFloat() * 2.5f,
                                sizeDp = (64 + random.nextInt(48)).dp,
                                swayDp = (16 + random.nextInt(24)).dp,
                                swayPhase = random.nextFloat() * 2f * PI.toFloat(),
                                rotationDeg = -8f + random.nextFloat() * 16f,
                                horizontalJitter = -0.5f + random.nextFloat(),
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
                seconds = seconds,
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
    seconds: FloatState,
    containerWidthPx: Float,
    containerHeightPx: Float,
    columnWidthPx: Float,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { spec.sizeDp.toPx() }
    val swayPx = with(density) { spec.swayDp.toPx() }
    val columnCenter = columnWidthPx * (spec.column + 0.5f)
    val jitter = columnWidthPx * 0.25f * spec.horizontalJitter

    Image(
        painter = painterResource(R.drawable.easter_balloon),
        contentDescription = null,
        modifier =
            Modifier
                .size(spec.sizeDp)
                .graphicsLayer {
                    val elapsed = seconds.floatValue - spec.startSeconds
                    if (elapsed < 0f) {
                        alpha = 0f
                        return@graphicsLayer
                    }
                    val p = (elapsed / spec.durationSeconds) % 1f
                    val sway = sin(p * 2f * PI.toFloat() + spec.swayPhase) * swayPx
                    translationX = (columnCenter + jitter + sway - sizePx / 2f).coerceIn(0f, containerWidthPx - sizePx)
                    translationY = containerHeightPx - p * (containerHeightPx + sizePx * 2f)
                    val fadeIn = (p * 5f).coerceAtMost(1f)
                    val fadeOut = ((1f - p) * 5f).coerceAtMost(1f)
                    alpha = (fadeIn * fadeOut).coerceIn(0f, 1f)
                    rotationZ = spec.rotationDeg + sin(p * 4f * PI.toFloat() + spec.swayPhase) * 6f
                },
    )
}
