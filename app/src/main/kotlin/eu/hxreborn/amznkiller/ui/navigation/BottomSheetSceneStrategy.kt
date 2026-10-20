package eu.hxreborn.amznkiller.ui.navigation

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

@OptIn(ExperimentalMaterial3Api::class)
private data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val overlaidEntries: List<NavEntry<T>> get() = previousEntries

    override val entries: List<NavEntry<T>> = listOf(entry)

    private var sheetState: SheetState? = null

    override suspend fun onRemove() {
        sheetState?.hide()
    }

    override val content: @Composable () -> Unit = {
        val lifecycleOwner = rememberLifecycleOwner()
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        sheetState = state
        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = state,
            modifier = Modifier.statusBarsPadding(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                entry.Content()
            }
        }
    }
}

class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull() ?: return null
        if (BottomSheetKey !in last.metadata) return null
        @Suppress("UNCHECKED_CAST")
        return BottomSheetScene(last.contentKey as T, entries.dropLast(1), last, onBack)
    }

    private object BottomSheetKey : NavMetadataKey<Boolean>

    companion object {
        fun bottomSheet(): Map<String, Any> = metadata { put(BottomSheetKey, true) }
    }
}
