package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens

private val GRAYSCALE =
    ColorFilter.colorMatrix(
        ColorMatrix().apply { setToSaturation(0f) },
    )

@Composable
internal fun TargetAppsCard(
    amazonPackage: String?,
    amazonInfo: Triple<ImageBitmap, String, String?>?,
    webViewInfo: Triple<ImageBitmap, String, String?>?,
    surface: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shape = Tokens.CardShape
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = surface, shape = shape)
                .clip(shape)
                .padding(16.dp),
    ) {
        TargetRow(amazonInfo) {
            if (amazonInfo != null) {
                IconButton(
                    onClick = {
                        amazonPackage?.let { context.packageManager.getLaunchIntentForPackage(it) }?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(it)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.dashboard_launch),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        TargetRow(webViewInfo, isGrayscale = true)
    }
}

@Composable
internal fun TargetRow(
    info: Triple<ImageBitmap, String, String?>?,
    isGrayscale: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (info != null) {
            Image(
                bitmap = info.first,
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
                text = info?.second ?: stringResource(R.string.target_title),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    if (info != null) {
                        "v${info.third}"
                    } else {
                        stringResource(R.string.dashboard_not_installed)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (info == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
            surface = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
