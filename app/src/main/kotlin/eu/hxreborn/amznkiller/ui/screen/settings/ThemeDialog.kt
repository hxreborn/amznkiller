package eu.hxreborn.amznkiller.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.theme.Tokens
import android.R as AndroidR

@Composable
internal fun ThemeDialog(
    currentConfig: DarkThemeConfig,
    onSelect: (DarkThemeConfig) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = DarkThemeConfig.entries
    val labels =
        listOf(
            stringResource(R.string.settings_theme_system),
            stringResource(R.string.settings_theme_light),
            stringResource(R.string.settings_theme_dark),
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(Tokens.ListRowHeight)
                                .selectable(
                                    selected = option == currentConfig,
                                    onClick = { onSelect(option) },
                                    role = Role.RadioButton,
                                ).padding(horizontal = Tokens.SpacingLg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == currentConfig,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(Tokens.SpacingLg))
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(AndroidR.string.cancel))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ThemeDialogPreview() {
    PreviewWrapper {
        ThemeDialog(
            currentConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            onSelect = {},
            onDismiss = {},
        )
    }
}
