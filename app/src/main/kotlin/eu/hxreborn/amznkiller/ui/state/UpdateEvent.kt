package eu.hxreborn.amznkiller.ui.state

sealed interface UpdateEvent {
    data class Updated(
        val added: Int,
        val removed: Int,
    ) : UpdateEvent

    data object UpToDate : UpdateEvent

    data class Error(
        val message: String,
    ) : UpdateEvent
}
