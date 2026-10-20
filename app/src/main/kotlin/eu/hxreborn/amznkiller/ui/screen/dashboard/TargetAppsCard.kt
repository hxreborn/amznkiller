package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.AmznKillerSurfaceDefaults
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.groupedItem

@Immutable
internal data class TargetAppData(
    val icon: ImageBitmap,
    val label: String,
    val versionName: String?,
)

private val GRAYSCALE =
    ColorFilter.colorMatrix(
        ColorMatrix().apply { setToSaturation(0f) },
    )

@Composable
internal fun TargetAppsCard(
    amazonPackage: String?,
    amazonInfo: TargetAppData?,
    webViewInfo: TargetAppData?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val color = AmznKillerSurfaceDefaults.cardContainerColor
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(Tokens.PreferenceItemGap),
    ) {
        TargetRow(amazonInfo, Modifier.groupedItem(color, count = 2, index = 0)) {
            if (amazonInfo != null) {
                FilledTonalIconButton(
                    onClick = {
                        amazonPackage?.let { context.packageManager.getLaunchIntentForPackage(it) }?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    },
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.dashboard_launch),
                    )
                }
            }
        }
        TargetRow(webViewInfo, Modifier.groupedItem(color, count = 2, index = 1), isGrayscale = true)
    }
}

@Composable
internal fun TargetRow(
    info: TargetAppData?,
    modifier: Modifier = Modifier,
    isGrayscale: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (info != null) {
            Image(
                bitmap = info.icon,
                contentDescription = null,
                colorFilter = if (isGrayscale) GRAYSCALE else null,
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info?.label ?: stringResource(R.string.target_title),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    if (info != null) {
                        "v${info.versionName}"
                    } else {
                        stringResource(R.string.dashboard_not_installed)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = if (info == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

@PreviewLightDark
@Composable
private fun TargetAppsCardNotInstalledPreview() {
    PreviewWrapper {
        TargetAppsCard(
            amazonPackage = null,
            amazonInfo = null,
            webViewInfo = null,
        )
    }
}
