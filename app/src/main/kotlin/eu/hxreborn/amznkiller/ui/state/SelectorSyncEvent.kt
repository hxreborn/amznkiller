package eu.hxreborn.amznkiller.ui.state

sealed interface SelectorSyncEvent {
    data class Updated(
        val added: Int,
        val removed: Int,
    ) : SelectorSyncEvent

    data object UpToDate : SelectorSyncEvent

    data class Error(
        val message: String,
    ) : SelectorSyncEvent
}

data class SelectorSyncOutcome(
    val event: SelectorSyncEvent,
    val id: Long = System.nanoTime(),
)
