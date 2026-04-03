package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens

@Composable
internal fun MetricsGrid(
    injectionEnabled: Boolean,
    selectorCount: Int,
    surface: Color,
    modifier: Modifier = Modifier,
) {
    val shape = Tokens.CardShape
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .background(color = surface, shape = shape)
                    .clip(shape)
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = if (injectionEnabled) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (injectionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.dashboard_method),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    if (injectionEnabled) {
                        stringResource(R.string.dashboard_webview_injection)
                    } else {
                        stringResource(R.string.dashboard_injection_paused)
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .background(color = surface, shape = shape)
                    .clip(shape)
                    .padding(16.dp)
                    .then(if (injectionEnabled) Modifier else Modifier.alpha(0.38f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Rule,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.config_active_selectors),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    if (injectionEnabled) {
                        stringResource(R.string.dashboard_rules_count, selectorCount)
                    } else {
                        stringResource(R.string.dashboard_no_active_rules)
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MetricsGridPreview() {
    PreviewWrapper {
        MetricsGrid(
            injectionEnabled = true,
            selectorCount = 42,
            surface = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
