package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.AmznKillerSurfaceDefaults
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.groupedItem

@Composable
internal fun SystemEnvironmentCard(
    isXposedActive: Boolean,
    frameworkVersion: String?,
    modifier: Modifier = Modifier,
) {
    val color = AmznKillerSurfaceDefaults.cardContainerColor
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(Tokens.PreferenceItemGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().groupedItem(color, count = 3, index = 0).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Extension,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isXposedActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.env_xposed),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text =
                        if (isXposedActive) {
                            stringResource(R.string.env_xposed_active, frameworkVersion ?: "Unknown")
                        } else {
                            stringResource(R.string.env_xposed_inactive)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isXposedActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().groupedItem(color, count = 3, index = 1).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = Build.MODEL,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${Build.MANUFACTURER} · ${Build.DEVICE}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().groupedItem(color, count = 3, index = 2).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Android,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Android ${Build.VERSION.RELEASE}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "API ${Build.VERSION.SDK_INT}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SystemEnvironmentCardPreview() {
    PreviewWrapper {
        SystemEnvironmentCard(
            isXposedActive = true,
            frameworkVersion = "LSPosed v1.11.0",
        )
    }
}
