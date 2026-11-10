package eu.hxreborn.amznkiller.ui.screen.about

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.contributors.ContributorRow
import eu.hxreborn.amznkiller.contributors.DIFF_BLOCK_COUNT
import eu.hxreborn.amznkiller.ui.preview.PreviewFixtures
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.theme.extendedColors
import java.text.NumberFormat

private val AvatarSize = 40.dp
private val StatRowGap = 2.dp
private val OwnerBadgeHeight = 20.dp
private val ContributorTitleHeight = 24.dp

private val OwnerBadgeText =
    TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
    )
private val DiffBlockSize = 9.dp
private val DiffBlockGap = 2.dp
private val DiffHatchSpacing = 3.dp
private val DiffHatchStroke = 1.5.dp
private const val HATCH_BACKGROUND_ALPHA = 0.25f
private const val TABULAR_FIGURES = "tnum"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val AvatarPolygons =
    listOf(
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Cookie7Sided,
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Cookie4Sided,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ContributorsGroup(
    contributors: List<ContributorRow>,
    onOpenUrl: (String) -> Unit,
) {
    if (contributors.isEmpty()) return
    val avatarShapes = AvatarPolygons.map { it.toShape() }
    Spacer(Modifier.height(SectionGap))
    Group(stringResource(R.string.contributors), contributors.size) { index, shape ->
        val row = contributors[index]
        ContributorTile(row, avatarShapes[index % avatarShapes.size], shape) { onOpenUrl(row.url) }
    }
}

@Composable
private fun ContributorTile(
    row: ContributorRow,
    avatarShape: Shape,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    val numbers = remember { NumberFormat.getIntegerInstance() }
    val addedColor = MaterialTheme.extendedColors.diffAdded
    val removedColor = MaterialTheme.colorScheme.error
    val stat =
        remember(row, addedColor, removedColor) {
            val tabular = SpanStyle(fontFeatureSettings = TABULAR_FIGURES)
            buildAnnotatedString {
                withStyle(tabular.copy(color = addedColor)) { append("+${numbers.format(row.added)}") }
                append(" ")
                withStyle(tabular.copy(color = removedColor)) { append("−${numbers.format(row.removed)}") }
            }
        }
    val description =
        stringResource(
            R.string.contributor_stat_a11y,
            pluralStringResource(R.plurals.contributor_lines_added_a11y, row.added, row.added),
            pluralStringResource(R.plurals.contributor_lines_removed_a11y, row.removed, row.removed),
        )
    Tile(shape = shape, onClick = onClick) {
        Avatar(row, avatarShape)
        Column(Modifier.weight(1f)) {
            Row(
                Modifier.heightIn(min = ContributorTitleHeight),
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    row.login,
                    style = TileTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (row.owner) OwnerBadge()
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(StatRowGap),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stat,
                    style = TileSupporting,
                    modifier = Modifier.semantics { contentDescription = description },
                )
                DiffBlocks(row.greenBlocks, row.redBlocks)
            }
        }
        Box(Modifier.width(ChevronSlot), contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OwnerBadge() {
    Surface(
        modifier = Modifier.height(OwnerBadgeHeight),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(Modifier.padding(horizontal = Tokens.SpacingSm), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.contributor_owner),
                style = OwnerBadgeText.merge(MaterialTheme.typography.labelSmall),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Avatar(
    row: ContributorRow,
    shape: Shape,
) {
    val bitmap = row.avatar
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(AvatarSize).clip(shape),
        )
    } else {
        Box(
            Modifier.size(AvatarSize).clip(shape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                row.login.first().uppercase(),
                style = TileTitle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun DiffBlocks(
    green: Int,
    red: Int,
) {
    val addedColor = MaterialTheme.extendedColors.diffAdded
    val removedColor = MaterialTheme.colorScheme.error
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val density = LocalDensity.current
    val hatch =
        remember(density) {
            with(density) {
                val block = DiffBlockSize.toPx()
                val spacing = DiffHatchSpacing.toPx()
                Path().apply {
                    var offset = -block
                    while (offset < block) {
                        moveTo(offset, block)
                        lineTo(offset + block, 0f)
                        offset += spacing
                    }
                }
            }
        }
    val hatchStroke = remember(density) { Stroke(width = with(density) { DiffHatchStroke.toPx() }) }
    Canvas(
        Modifier
            .size(DiffBlockSize * DIFF_BLOCK_COUNT + DiffBlockGap * (DIFF_BLOCK_COUNT - 1), DiffBlockSize)
            .clearAndSetSemantics {},
    ) {
        val block = DiffBlockSize.toPx()
        val step = block + DiffBlockGap.toPx()
        repeat(DIFF_BLOCK_COUNT) { index ->
            val left = index * step
            when {
                index < green -> {
                    drawRect(addedColor, Offset(left, 0f), Size(block, block))
                }

                index < green + red -> {
                    translate(left, 0f) {
                        clipRect(0f, 0f, block, block) {
                            drawRect(removedColor.copy(alpha = HATCH_BACKGROUND_ALPHA), size = Size(block, block))
                            drawPath(hatch, removedColor, style = hatchStroke)
                        }
                    }
                }

                else -> {
                    drawRect(emptyColor, Offset(left, 0f), Size(block, block))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ContributorsGroupPreview() {
    PreviewWrapper {
        ContributorsGroup(PreviewFixtures.contributors) {}
    }
}
