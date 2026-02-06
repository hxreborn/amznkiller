package eu.hxreborn.amznkiller.ui.animation

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlin.math.PI

@Composable
fun rememberWavePhase(): Float {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(waveCycleSpec()),
        label = "wave_phase",
    )
    return phase
}
