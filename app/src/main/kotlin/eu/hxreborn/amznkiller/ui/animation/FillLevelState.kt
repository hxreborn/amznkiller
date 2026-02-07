package eu.hxreborn.amznkiller.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class FillLevelState internal constructor(
    private val animatable: Animatable<Float, *>,
) {
    val value: Float get() = animatable.value
}

@Composable
fun rememberFillLevelState(
    isRefreshing: Boolean,
    isError: Boolean = false,
    onStateChange: (AnimationState) -> Unit = {},
): FillLevelState {
    val animatable = remember { Animatable(0f) }
    val onStateChangeUpdated by rememberUpdatedState(onStateChange)
    val isErrorUpdated by rememberUpdatedState(isError)
    val idleEmitted = remember { AtomicBoolean(false) }

    LaunchedEffect(isRefreshing) {
        if (idleEmitted.compareAndSet(false, true)) {
            onStateChangeUpdated(AnimationState.Idle)
        }

        if (isRefreshing) {
            animatable.snapTo(0f)
            val job =
                launch {
                    snapshotFlow { animatable.value }
                        .distinctUntilChanged()
                        .collect { value -> onStateChangeUpdated(AnimationState.Filling(value)) }
                }
            try {
                animatable.animateTo(FILL_SLOW_TARGET, fillSlowSpec())
            } finally {
                job.cancelAndJoin()
            }
        } else {
            if (animatable.value > 0f) {
                val job =
                    launch {
                        snapshotFlow { animatable.value }
                            .distinctUntilChanged()
                            .collect { value -> onStateChangeUpdated(AnimationState.Finishing(value)) }
                    }
                try {
                    if (animatable.value < FILL_MIN_TARGET) {
                        val remainingMs =
                            ((FILL_MIN_TARGET - animatable.value) / FILL_MIN_TARGET * 1500)
                                .toInt()
                        animatable.animateTo(FILL_MIN_TARGET, fillCatchUpSpec(remainingMs))
                    }
                    if (isErrorUpdated) {
                        val drainMs = (animatable.value * 800).toInt()
                        animatable.animateTo(0f, fillDrainSpec(drainMs))
                    } else {
                        animatable.animateTo(1f, fillFinishSpec())
                    }
                } finally {
                    job.cancelAndJoin()
                }
            }
            onStateChangeUpdated(AnimationState.Completed)
        }
    }

    return remember { FillLevelState(animatable) }
}
