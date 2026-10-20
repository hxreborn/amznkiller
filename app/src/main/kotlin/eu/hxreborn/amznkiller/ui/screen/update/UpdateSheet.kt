package eu.hxreborn.amznkiller.ui.screen.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.update.InstallStep
import eu.hxreborn.amznkiller.update.Release

private val BlockRadius = 16.dp
private val ActionHeight = 56.dp
private const val HASH_HEAD = 8
private const val HASH_TAIL = 4

class UpdateActions(
    val onInstall: () -> Unit,
    val onInstallAnyway: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onGitHub: () -> Unit,
    val onCancel: () -> Unit,
)

private val metaStyle: TextStyle
    @Composable get() = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateSheet(
    release: Release,
    step: InstallStep,
    actions: UpdateActions,
) {
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val size = release.asset?.size?.let(::bytesLabel)
        SheetHeader(
            stringResource(R.string.update_sheet_title, release.versionName),
            size?.let { stringResource(R.string.update_from_github, it) },
        )
        when (step) {
            InstallStep.Idle, InstallStep.NeedsPermission -> {
                Notes(stringResource(R.string.notes_new), release.notesNew)
                Notes(stringResource(R.string.notes_fixed), release.notesFixed)
                Notes(stringResource(R.string.notes_other), release.notesOther)
                if (step == InstallStep.NeedsPermission) {
                    Block(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurface, Icons.Outlined.Shield) {
                        Text(stringResource(R.string.update_allow_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.update_allow_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Primary(stringResource(R.string.update_open_settings), Icons.AutoMirrored.Outlined.OpenInNew, actions.onOpenSettings)
                } else {
                    if (release.asset == null) {
                        Text(
                            stringResource(R.string.update_no_asset),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Primary(
                        stringResource(R.string.update_install),
                        Icons.Outlined.Download,
                        actions.onInstall,
                        enabled =
                            release.asset != null,
                    )
                    GitHubLink(actions.onGitHub)
                }
            }

            is InstallStep.Downloading -> {
                val fraction = if (step.total > 0) step.bytes.toFloat() / step.total else 0f
                LinearWavyProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                Row {
                    Text(
                        stringResource(R.string.update_progress, bytesLabel(step.bytes), bytesLabel(step.total)),
                        style = metaStyle,
                        modifier = Modifier.weight(1f),
                    )
                    Text("${(fraction * 100).toInt()}%", style = metaStyle)
                }
                VerifyLog(release.asset?.sha256, step.partialHash, hashing = true, installed = null, file = null, signerChecking = false)
                OutlinedButton(onClick = actions.onCancel, shapes = ButtonDefaults.shapes(), modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(android.R.string.cancel))
                }
            }

            is InstallStep.Verifying -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    LoadingIndicator(Modifier.size(32.dp))
                    Text(stringResource(R.string.update_verifying), style = MaterialTheme.typography.bodyMedium)
                }
                VerifyLog(
                    step.expected,
                    step.actual,
                    hashing = false,
                    installed = step.installedSigner,
                    file = step.fileSigner,
                    signerChecking = step.fileSigner == null,
                )
            }

            is InstallStep.Rejected -> {
                if (step.releaseKey) {
                    Block(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface, Icons.Outlined.Key) {
                        Text(stringResource(R.string.update_release_signer), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.update_install_anyway_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val body =
                        when (step.cause) {
                            InstallStep.Rejected.Cause.CHECKSUM -> R.string.update_bad_checksum
                            InstallStep.Rejected.Cause.SIGNER -> R.string.update_bad_signer
                            InstallStep.Rejected.Cause.PACKAGE -> R.string.update_bad_package
                            InstallStep.Rejected.Cause.NO_CHECKSUM -> R.string.update_no_checksum
                        }
                    Block(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Outlined.GppBad) {
                        Text(stringResource(R.string.update_not_installed), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                when (step.cause) {
                    InstallStep.Rejected.Cause.CHECKSUM -> {
                        VerifyLog(
                            step.expected,
                            step.actual,
                            hashing = false,
                            installed = null,
                            file = null,
                            signerChecking = false,
                            failed = true,
                        )
                    }

                    InstallStep.Rejected.Cause.SIGNER -> {
                        VerifyLog(
                            step.checksum,
                            step.checksum,
                            hashing = false,
                            installed = step.expected,
                            file = step.actual,
                            signerChecking = false,
                            failed = true,
                        )
                    }

                    else -> {}
                }
                if (step.releaseKey) {
                    Primary(stringResource(R.string.update_install_anyway), Icons.Outlined.Download, actions.onInstallAnyway)
                } else {
                    Primary(stringResource(R.string.update_download_again), Icons.Outlined.Download, actions.onInstall)
                }
                GitHubLink(actions.onGitHub)
            }

            is InstallStep.DownloadFailed -> {
                Block(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Outlined.ErrorOutline) {
                    Text(
                        stringResource(R.string.update_download_failed, bytesLabel(step.bytes)),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(stringResource(R.string.update_download_failed_body), style = MaterialTheme.typography.bodyMedium)
                    Text(step.reason, style = metaStyle)
                }
                Primary(stringResource(R.string.update_try_again), Icons.Outlined.Refresh, actions.onInstall)
                GitHubLink(actions.onGitHub)
            }

            InstallStep.Installing -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    LoadingIndicator(Modifier.size(32.dp))
                    Text(stringResource(R.string.update_waiting_installer), style = MaterialTheme.typography.bodyMedium)
                }
            }

            is InstallStep.InstallFailed -> {
                Block(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Outlined.ErrorOutline) {
                    Text(stringResource(R.string.update_install_failed, step.message), style = MaterialTheme.typography.bodyMedium)
                }
                Primary(stringResource(R.string.update_try_again), Icons.Outlined.Refresh, actions.onInstall)
                GitHubLink(actions.onGitHub)
            }
        }
    }
}

@Composable
internal fun SheetHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(Modifier.padding(top = 4.dp, bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Notes(
    title: String,
    lines: List<String>,
) {
    if (lines.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        lines.forEach { NoteLine(it) }
    }
}

@Composable
private fun Block(
    container: Color,
    content: Color,
    icon: ImageVector,
    body: @Composable ColumnScope.() -> Unit,
) {
    Surface(shape = RoundedCornerShape(BlockRadius), color = container, contentColor = content, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null)
            Column { body() }
        }
    }
}

@Composable
private fun Primary(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth().heightIn(min = ActionHeight),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(label)
    }
}

@Composable
private fun GitHubLink(onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.update_view_github))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    }
}

internal fun bytesLabel(bytes: Long): String {
    val mb = bytes / 1_048_576.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%d KB".format(bytes / 1024)
}

private fun shortHash(hash: String?): String =
    when {
        hash.isNullOrEmpty() -> "?"
        hash.length <= HASH_HEAD + HASH_TAIL -> hash
        else -> "${hash.take(HASH_HEAD)}…${hash.takeLast(HASH_TAIL)}"
    }

@Composable
private fun VerifyLog(
    expected: String?,
    actual: String?,
    hashing: Boolean,
    installed: String?,
    file: String?,
    signerChecking: Boolean,
    failed: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (expected != null || actual != null) {
                Text(stringResource(R.string.vlog_checksum), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                LogRow(stringResource(R.string.vlog_release), shortHash(expected), null)
                val hashState =
                    when {
                        hashing -> LogState.Busy(stringResource(R.string.vlog_hashing))
                        expected != null && expected.equals(actual, ignoreCase = true) -> LogState.Match
                        else -> LogState.Mismatch
                    }
                LogRow(
                    stringResource(R.string.vlog_file),
                    if (hashing) actual.orEmpty().take(HASH_HEAD + 4) + "…" else shortHash(actual),
                    hashState,
                )
            }
            if (installed != null || file != null || hashing || !failed) {
                Text(
                    stringResource(R.string.vlog_signing_key),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                if (hashing) {
                    LogRow(stringResource(R.string.vlog_file), stringResource(R.string.vlog_waiting), null, dim = true)
                } else {
                    LogRow(stringResource(R.string.vlog_installed), shortHash(installed), null)
                    val signerState =
                        when {
                            signerChecking -> LogState.Busy(stringResource(R.string.vlog_checking))
                            installed != null && installed == file -> LogState.Match
                            else -> LogState.Mismatch
                        }
                    LogRow(stringResource(R.string.vlog_file), shortHash(file), signerState)
                }
            }
        }
    }
}

private sealed interface LogState {
    data object Match : LogState

    data object Mismatch : LogState

    data class Busy(
        val label: String,
    ) : LogState
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LogRow(
    key: String,
    value: String,
    state: LogState?,
    dim: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(key, style = metaStyle, color = scheme.onSurfaceVariant, modifier = Modifier.width(72.dp))
        Text(value, style = metaStyle, color = if (dim) scheme.onSurfaceVariant else scheme.onSurface, modifier = Modifier.weight(1f))
        when (state) {
            LogState.Match -> {
                StateLabel(Icons.Outlined.CheckCircle, stringResource(R.string.vlog_match), scheme.primary)
            }

            LogState.Mismatch -> {
                StateLabel(Icons.Outlined.ErrorOutline, stringResource(R.string.vlog_mismatch), scheme.error)
            }

            is LogState.Busy -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LoadingIndicator(Modifier.size(18.dp))
                    Text(state.label, style = metaStyle, color = scheme.onSurfaceVariant)
                }
            }

            null -> {}
        }
    }
}

@Composable
private fun StateLabel(
    icon: ImageVector,
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, style = metaStyle, color = color)
    }
}

@PreviewLightDark
@Composable
private fun UpdateSheetPreview() {
    PreviewWrapper {
        UpdateSheet(
            previewRelease,
            InstallStep.Rejected(InstallStep.Rejected.Cause.SIGNER, "3df5e77bb596", "32b98dc709a7", "4b45d372b09d"),
            UpdateActions({}, {}, {}, {}, {}),
        )
    }
}
