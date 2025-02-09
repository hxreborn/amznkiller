package eu.hxreborn.amznkiller.ui.state

import androidx.annotation.StringRes

sealed interface SelectorSyncEvent {
    data class Updated(
        val added: Int,
        val removed: Int,
    ) : SelectorSyncEvent

    data object UpToDate : SelectorSyncEvent

    data class Error(
        @StringRes val messageResId: Int = 0,
        val fallback: String? = null,
    ) : SelectorSyncEvent
}

data class SelectorSyncOutcome(
    val event: SelectorSyncEvent,
    val id: Long = System.nanoTime(),
)
