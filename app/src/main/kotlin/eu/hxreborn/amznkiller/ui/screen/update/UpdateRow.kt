package eu.hxreborn.amznkiller.ui.screen.update

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.util.relativeTime
import eu.hxreborn.amznkiller.ui.viewmodel.UpdateUiState
import eu.hxreborn.amznkiller.update.ReleaseCheck

class UpdateSettingsActions(
    val onCheck: () -> Unit,
    val onUpdate: () -> Unit,
    val onAutoCheck: (Boolean) -> Unit,
)

@Composable
fun UpdateSettingsActions.withHaptics(release: ReleaseCheck): UpdateSettingsActions {
    val haptics = LocalHapticFeedback.current
    var awaitingCheck by remember { mutableStateOf(false) }
    LaunchedEffect(release) {
        if (!awaitingCheck) return@LaunchedEffect
        when (release) {
            is ReleaseCheck.Done -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            is ReleaseCheck.Failed -> haptics.performHapticFeedback(HapticFeedbackType.Reject)
            else -> return@LaunchedEffect
        }
        awaitingCheck = false
    }
    return UpdateSettingsActions(
        onCheck = {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            awaitingCheck = true
            onCheck()
        },
        onUpdate = onUpdate,
        onAutoCheck = onAutoCheck,
    )
}

fun UpdateUiState.settingsRowAction(actions: UpdateSettingsActions): () -> Unit =
    when {
        available != null -> actions.onUpdate
        release is ReleaseCheck.Checking -> ({})
        else -> actions.onCheck
    }

@Composable
fun UpdateRowSummary(state: UpdateUiState) {
    val release = state.release
    val available = state.available
    val failed = release is ReleaseCheck.Failed && available == null
    Text(
        when {
            available != null -> {
                stringResource(R.string.update_available_short, available.versionName)
            }

            release is ReleaseCheck.Checking -> {
                stringResource(R.string.update_checking)
            }

            release is ReleaseCheck.Failed -> {
                release.lastCheckedAt?.let { stringResource(R.string.update_check_failed, relativeTime(it)) }
                    ?: stringResource(R.string.update_check_failed_never)
            }

            release is ReleaseCheck.Done -> {
                stringResource(R.string.update_up_to_date, relativeTime(release.checkedAt))
            }

            else -> {
                stringResource(R.string.update_never_checked)
            }
        },
        color =
            when {
                failed -> MaterialTheme.colorScheme.error
                available != null -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateRowAction(
    state: UpdateUiState,
    actions: UpdateSettingsActions,
) {
    val release = state.release
    when {
        state.available != null -> {
            FilledTonalButton(onClick = actions.onUpdate, modifier = Modifier.padding(end = 16.dp).clearAndSetSemantics { }) {
                Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.update))
            }
        }

        release is ReleaseCheck.Checking -> {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { LoadingIndicator(Modifier.size(32.dp)) }
        }

        else -> {
            IconButton(onClick = actions.onCheck, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    if (release is ReleaseCheck.Failed) Icons.Outlined.ErrorOutline else Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.update_check),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
