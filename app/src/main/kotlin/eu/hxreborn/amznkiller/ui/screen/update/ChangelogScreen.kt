package eu.hxreborn.amznkiller.ui.screen.update

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material.icons.outlined.Webhook
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import eu.hxreborn.amznkiller.ui.viewmodel.ChangelogState
import eu.hxreborn.amznkiller.update.Release
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val ChipRadius = 8.dp
private val ReleaseCardPadding = 12.dp
private val AreaIconSize = 16.dp
private val BadgeHeight = 22.dp
private val BadgeIconSize = 14.dp
private val NoteMinHeight = 40.dp
private val ScreenGutter = 16.dp

private enum class NoteArea(
    val scopes: Set<String?>,
    @param:StringRes val title: Int,
    val icon: ImageVector,
) {
    BLOCKING(setOf("selectors", "scripts"), R.string.changelog_area_blocking, Icons.Outlined.Block),
    HOOKS(setOf("hook", "hooks", "module", "compat", "log"), R.string.changelog_area_hooks, Icons.Outlined.Webhook),
    SETTINGS(setOf("prefs"), R.string.changelog_area_settings, Icons.Outlined.Tune),
    UPDATES(setOf("update"), R.string.changelog_area_updates, Icons.Outlined.SystemUpdate),
    INTERFACE(setOf("ui"), R.string.changelog_area_ui, Icons.Outlined.Palette),
    APP(setOf("app"), R.string.changelog_area_app, Icons.Outlined.Smartphone),
    OTHER(emptySet(), R.string.changelog_area_other, Icons.Outlined.Code),
    ;

    companion object {
        fun of(scope: String?): NoteArea = entries.firstOrNull { scope in it.scopes } ?: OTHER
    }
}

private fun formatIso(isoDate: String?): String? =
    isoDate?.let {
        runCatching {
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .format(Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate())
        }.getOrNull()
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangelogScreen(
    state: ChangelogState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val releases = (state as? ChangelogState.Loaded)?.releases.orEmpty()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val atEnd by remember { derivedStateOf { !listState.canScrollForward } }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.changelog))
                        if (state is ChangelogState.Loaded) {
                            Text(
                                pluralStringResource(R.plurals.changelog_count, releases.size, releases.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ChangelogState.Loading -> {
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }

                is ChangelogState.Failed -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(ScreenGutter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.changelog_failed), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            state.reason,
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onRetry) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.update_try_again))
                        }
                    }
                }

                is ChangelogState.Loaded -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = ScreenGutter, end = ScreenGutter, top = 8.dp, bottom = navBottom + 88.dp),
                    ) {
                        itemsIndexed(releases, key = { _, release -> release.versionName }) { index, release ->
                            if (index > 0) Spacer(Modifier.height(Tokens.PreferenceItemGap))
                            ReleaseSection(release, shapeForPosition(releases.size, index))
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = listState.canScrollBackward || listState.canScrollForward,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = navBottom + 16.dp),
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) + scaleIn(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleOut(MaterialTheme.motionScheme.fastSpatialSpec()),
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            if (atEnd) listState.scrollToItem(0) else listState.scrollToItem(releases.lastIndex, Int.MAX_VALUE)
                        }
                    },
                ) {
                    Icon(
                        if (atEnd) Icons.Outlined.VerticalAlignTop else Icons.Outlined.VerticalAlignBottom,
                        contentDescription = stringResource(if (atEnd) R.string.changelog_jump_top else R.string.changelog_jump_bottom),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReleaseSection(
    release: Release,
    shape: Shape,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .padding(ReleaseCardPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 4.dp).semantics { heading() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(release.versionName, style = MaterialTheme.typography.titleLargeEmphasized)
            Text(
                formatIso(release.publishedAt).orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (release.versionName == BuildConfig.VERSION_NAME) InstalledChip()
        }
        ReleaseNotes(release)
    }
}

@Composable
private fun InstalledChip() {
    Surface(shape = RoundedCornerShape(ChipRadius), color = MaterialTheme.colorScheme.primary) {
        Text(
            stringResource(R.string.changelog_installed),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ReleaseNotes(release: Release) {
    val byArea = release.notes.groupBy { NoteArea.of(it.scope) }
    Column {
        NoteArea.entries.forEach { area ->
            val notes = byArea[area]?.sortedBy { it.kind.ordinal } ?: return@forEach
            AreaHeader(area)
            notes.forEach { NoteRow(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AreaHeader(area: NoteArea) {
    Row(
        Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 2.dp).semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            area.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(AreaIconSize),
        )
        Text(
            stringResource(area.title).uppercase(),
            style = MaterialTheme.typography.labelMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoteRow(note: Release.Note) {
    val uriHandler = LocalUriHandler.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = NoteMinHeight)
            .clip(RoundedCornerShape(ChipRadius))
            .then(
                note.commitUrl?.let {
                    Modifier.clickable(onClickLabel = stringResource(R.string.changelog_open_commit), role = Role.Button) {
                        uriHandler.openUri(it)
                    }
                } ?: Modifier,
            ).padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KindBadge(note)
        Text(note.text.replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KindBadge(note: Release.Note) {
    val scheme = MaterialTheme.colorScheme
    val (label, icon, colors) =
        when (note.kind) {
            Release.Note.Kind.NEW -> {
                Triple(stringResource(R.string.notes_new), Icons.Outlined.AutoAwesome, scheme.primaryContainer to scheme.onPrimaryContainer)
            }

            Release.Note.Kind.FIXED -> {
                Triple(stringResource(R.string.notes_fixed), Icons.Outlined.Build, scheme.tertiaryContainer to scheme.onTertiaryContainer)
            }

            Release.Note.Kind.OTHER -> {
                Triple(note.section, Icons.Outlined.Code, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }
        }
    Surface(shape = RoundedCornerShape(ChipRadius), color = colors.first, contentColor = colors.second) {
        Row(
            Modifier.heightIn(min = BadgeHeight).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(BadgeIconSize))
            Text(label, style = MaterialTheme.typography.labelSmallEmphasized, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WhatsNewSheet(
    state: ChangelogState,
    onFullChangelog: () -> Unit,
    onClose: () -> Unit,
) {
    val installed = (state as? ChangelogState.Loaded)?.releases?.firstOrNull { it.versionName == BuildConfig.VERSION_NAME }
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SheetHeader(
            stringResource(R.string.whats_new_title, BuildConfig.VERSION_NAME),
            installed?.let { formatIso(it.publishedAt) }?.let { stringResource(R.string.whats_new_released, it) },
        )
        when {
            installed != null -> {
                ReleaseNotes(installed)
            }

            state is ChangelogState.Loading -> {
                LoadingIndicator(Modifier.align(Alignment.CenterHorizontally))
            }

            else -> {
                Text(
                    stringResource(if (state is ChangelogState.Failed) R.string.changelog_failed else R.string.whats_new_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SheetActions {
            TextButton(onClick = onClose, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.close))
            }
            FilledTonalButton(onClick = onFullChangelog, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.changelog_see_full))
            }
        }
    }
}

@Composable
private fun SheetActions(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun ChangelogScreenPreview() {
    PreviewWrapper {
        ChangelogScreen(ChangelogState.Loaded(listOf(previewRelease)), onRetry = {}, onBack = {})
    }
}
