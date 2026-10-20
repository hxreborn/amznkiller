package eu.hxreborn.amznkiller.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens

@Composable
fun ForceDarkModeButtons(
    selected: ForceDarkMode,
    onSelect: (ForceDarkMode) -> Unit,
) {
    ChoiceButtonGroup(
        options = ForceDarkMode.entries,
        selected = selected,
        onSelect = onSelect,
        label = {
            stringResource(
                when (it) {
                    ForceDarkMode.OFF -> R.string.settings_force_dark_off
                    ForceDarkMode.FOLLOW_SYSTEM -> R.string.settings_force_dark_follow_system
                    ForceDarkMode.ON -> R.string.settings_force_dark_on
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ChoiceButtonGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = Tokens.SpacingLg, end = Tokens.SpacingLg, bottom = Tokens.SpacingLg)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option == selected,
                onCheckedChange = { onSelect(option) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Text(
                    text = label(option),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = LocalTextStyle.current.fontSize),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChoiceButtonGroupPreview() {
    PreviewWrapper {
        ChoiceButtonGroup(
            options = listOf("Off", "System", "On"),
            selected = "System",
            onSelect = {},
            label = { it },
        )
    }
}
