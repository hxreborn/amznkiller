package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens

private val ChipMinHeight = 28.dp
private val ChipIconSize = 14.dp
private val ChipContentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DeltaChips(
    added: Int,
    removed: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val a11yLabel =
        remember(added, removed) {
            buildString {
                if (added > 0) {
                    append(
                        context.resources.getQuantityString(R.plurals.delta_added_a11y, added, added),
                    )
                }
                if (added > 0 && removed > 0) append(", ")
                if (removed > 0) {
                    append(
                        context.resources.getQuantityString(R.plurals.delta_removed_a11y, removed, removed),
                    )
                }
            }
        }

    AnimatedVisibility(
        visible = visible,
        enter =
            expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit =
            shrinkVertically(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = a11yLabel
                },
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (added > 0) {
                DeltaChip(
                    icon = Icons.Rounded.ArrowUpward,
                    target = added,
                    sign = '+',
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            if (removed > 0) {
                DeltaChip(
                    icon = Icons.Rounded.ArrowDownward,
                    target = removed,
                    sign = '-',
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeltaChip(
    icon: ImageVector,
    target: Int,
    sign: Char,
    containerColor: Color,
    contentColor: Color,
) {
    val anim = remember { Animatable(0f) }
    val countSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(target) {
        anim.animateTo(
            targetValue = target.toFloat(),
            animationSpec = countSpec,
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
        modifier =
            Modifier
                .clearAndSetSemantics {}
                .defaultMinSize(minHeight = ChipMinHeight),
    ) {
        Row(
            modifier = Modifier.padding(ChipContentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingXs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ChipIconSize),
                tint = contentColor,
            )
            Text(
                text = "$sign${anim.value.toInt()}",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DeltaChipsBothPreview() {
    PreviewWrapper {
        DeltaChips(added = 12, removed = 3, visible = true)
    }
}

@PreviewLightDark
@Composable
private fun DeltaChipsAddedOnlyPreview() {
    PreviewWrapper {
        DeltaChips(added = 7, removed = 0, visible = true)
    }
}

@PreviewLightDark
@Composable
private fun DeltaChipsRemovedOnlyPreview() {
    PreviewWrapper {
        DeltaChips(added = 0, removed = 4, visible = true)
    }
}
