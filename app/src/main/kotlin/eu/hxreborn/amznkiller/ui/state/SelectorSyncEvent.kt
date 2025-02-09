package eu.hxreborn.amznkiller.ui.state

import androidx.annotation.StringRes
import eu.hxreborn.amznkiller.R

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

fun SelectorSyncEvent.Error.resolveMessage(getString: (Int) -> String): String =
    if (messageResId != 0) getString(messageResId) else fallback ?: getString(R.string.snackbar_update_failed)

data class SelectorSyncOutcome(
    val event: SelectorSyncEvent,
    val id: Long = System.nanoTime(),
)
