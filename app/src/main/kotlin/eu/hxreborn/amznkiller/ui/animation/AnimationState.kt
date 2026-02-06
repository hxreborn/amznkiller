package eu.hxreborn.amznkiller.ui.animation

sealed interface AnimationState {
    data object Idle : AnimationState

    data class Filling(
        val value: Float,
    ) : AnimationState

    data class Finishing(
        val value: Float,
    ) : AnimationState

    data object Completed : AnimationState
}
