package eu.hxreborn.amznkiller.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.GraphicsLayerScope
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val DECAY = 0.8f
private const val DURATION_SECS = 5f
private const val EPSILON = 0.001f

class WobbleState {
    var scaleX by mutableFloatStateOf(1f)
        private set
    var scaleY by mutableFloatStateOf(1f)
        private set
    var rotation by mutableFloatStateOf(0f)
        private set
    var active by mutableStateOf(false)
        private set

    private var scaleAmp = 0f
    private var scaleFreq = 0f
    private var scalePhase = 0f
    private var rotAmp = 0f
    private var rotFreq = 0f
    private var rotPhase = 0f

    fun trigger() {
        scaleAmp = Random.nextFloat() * 0.04f + 0.04f
        scaleFreq = Random.nextFloat() * 2f + 3f
        scalePhase = Random.nextFloat() * (2f * PI.toFloat())
        rotAmp = Random.nextFloat() * 2f + 1f
        rotFreq = scaleFreq + Random.nextFloat() * 0.6f - 0.3f
        rotPhase = Random.nextFloat() * (2f * PI.toFloat())
        active = true
    }

    internal fun update(t: Float) {
        val envelope = exp(-DECAY * t)
        val twoPi = 2f * PI.toFloat()
        val s = envelope * scaleAmp * sin(twoPi * scaleFreq * t + scalePhase)
        scaleX = 1f + s
        scaleY = 1f - s
        rotation = envelope * rotAmp * sin(twoPi * rotFreq * t + rotPhase)
    }

    internal fun reset() {
        scaleX = 1f
        scaleY = 1f
        rotation = 0f
        active = false
    }

    internal companion object {
        const val DURATION = DURATION_SECS
        const val EPS = EPSILON
    }
}

fun GraphicsLayerScope.applyWobble(state: WobbleState) {
    scaleX = state.scaleX
    scaleY = state.scaleY
    rotationZ = state.rotation
}

@Composable
fun rememberWobbleState(): WobbleState {
    val state = remember { WobbleState() }
    LaunchedEffect(state.active) {
        if (!state.active) return@LaunchedEffect
        val startNanos = withFrameNanos { it }
        try {
            while (true) {
                val nanos = withFrameNanos { it }
                val t = (nanos - startNanos) / 1_000_000_000f
                if (t > WobbleState.DURATION) break
                state.update(t)
                if (exp(-DECAY * t) < WobbleState.EPS) break
            }
        } finally {
            state.reset()
        }
    }
    return state
}
