package eu.hxreborn.amznkiller.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper

@Composable
fun ControlCard(
    isRefreshing: Boolean,
    injectionEnabled: Boolean,
    onToggleInjection: (Boolean) -> Unit,
    onUpdate: () -> Unit,
    onOpenAmazon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.control_css_injection),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = injectionEnabled,
                    onCheckedChange = onToggleInjection,
                )
            }

            Spacer(Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onUpdate,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                enabled = !isRefreshing,
            ) {
                AnimatedContent(
                    targetState = isRefreshing,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "update_button",
                ) { refreshing ->
                    if (refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.control_check_updates))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onOpenAmazon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.control_open_amazon))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ControlCardPreview() {
    PreviewWrapper {
        ControlCard(
            isRefreshing = false,
            injectionEnabled = true,
            onToggleInjection = { },
            onUpdate = { },
            onOpenAmazon = { },
        )
    }
}
