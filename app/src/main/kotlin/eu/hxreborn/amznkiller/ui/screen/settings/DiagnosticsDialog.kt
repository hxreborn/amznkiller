package eu.hxreborn.amznkiller.ui.screen.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.getSystemService
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens

@Composable
internal fun DiagnosticsDialog(
    frameworkVersion: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val title = stringResource(R.string.settings_diagnostics)
    val notActive = stringResource(R.string.dashboard_module_inactive)
    val notInstalled = stringResource(R.string.dashboard_not_installed)
    val rows =
        remember(frameworkVersion) {
            val packageManager = context.packageManager
            val amazon =
                BuildConfig.TARGET_PACKAGES.firstNotNullOfOrNull { pkg ->
                    runCatching { packageManager.getPackageInfo(pkg, 0) }.getOrNull()
                }
            val webView = runCatching { WebView.getCurrentWebViewPackage() }.getOrNull()
            listOf(
                R.string.diagnostics_framework to (frameworkVersion ?: notActive),
                R.string.target_title to (amazon?.let { "${it.versionName} (${it.packageName})" } ?: notInstalled),
                R.string.diagnostics_webview to (webView?.let { "${it.versionName} (${it.packageName})" } ?: notInstalled),
                R.string.diagnostics_device to "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})",
                R.string.diagnostics_android to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                R.string.app_name to "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            ).map { (label, value) -> context.getString(label) to value }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(Tokens.SpacingMd)) {
                    rows.forEach { (label, value) ->
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(text = value, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val report = rows.joinToString("\n") { (label, value) -> "$label: $value" }
                    context.getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(title, report))
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(context, R.string.diagnostics_copied, Toast.LENGTH_SHORT).show()
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.diagnostics_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.diagnostics_close))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun DiagnosticsDialogPreview() {
    PreviewWrapper {
        DiagnosticsDialog(frameworkVersion = "LSPosed v1.11.0", onDismiss = {})
    }
}
