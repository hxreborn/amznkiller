package eu.hxreborn.amznkiller.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R

@Composable
fun ControlCard(
    isRefreshing: Boolean,
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
            Text(
                text = stringResource(R.string.control_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )

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
                Spacer(Modifier.padding(start = 4.dp))
                Text(stringResource(R.string.control_open_amazon))
            }
        }
    }
}
