package eu.hxreborn.amznkiller.ui.screen.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Troubleshoot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import eu.hxreborn.amznkiller.App
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.component.CelebrationOverlay
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.settings.DiagnosticsDialog
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val REPO = "hxreborn/amznkiller"
private const val REPO_URL = "https://github.com/$REPO"
private const val KOFI_URL = "https://ko-fi.com/hxreborn"
private const val LICENSE = "GPL-3.0"
private val ChevronSlot = 40.dp
private val CookieSize = 88.dp
private const val COOKIE_SPIN_MS = 8_000
private const val FULL_TURN = 360f
private const val HINT_MILLIS = 2_200L

private const val ADAPTIVE_ICON_ZOOM = 1.5f
private val ConnectedButtonHeight = 52.dp
private val SectionGap = 14.dp
private val ContentMaxWidth = 640.dp
private val CompactHeight = 480.dp
private val MinGutter = 16.dp
private val TileGap = 2.dp
private val TileStartPadding = 16.dp
private val TileEndPadding = 8.dp
private val TileVerticalPadding = 10.dp
private val TileMinHeight = 60.dp
private val TileSlotSpacing = 12.dp
private val TileOuterRadius = 20.dp
private val TileInnerRadius = 4.dp
private val RowGlyphSize = 24.dp
private const val SHAREHOLDER_TAPS = 7

private val TileTitle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
private val TileSupporting = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
private val Meta = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp)
private val ButtonText = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
private val Subheader = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)

class AboutActions(
    val onWhatsNew: () -> Unit,
    val onChangelog: () -> Unit,
    val onLicenses: () -> Unit,
    val onOpenUrl: (String) -> Unit,
)

@Composable
fun AboutScreen(
    framework: String?,
    actions: AboutActions,
    onBack: () -> Unit,
) {
    val shareholderEgg = rememberShareholderEgg()
    var iconCenterInRoot by remember { mutableStateOf(Offset.Zero) }
    DetailScaffold(
        title = stringResource(R.string.settings_about),
        onBack = onBack,
        overlay = {
            if (shareholderEgg.showCelebration) {
                CelebrationOverlay(
                    originInRoot = iconCenterInRoot,
                    startedAtNanos = shareholderEgg.startedAtNanos,
                    seed = shareholderEgg.seed,
                    firstTime = shareholderEgg.firstTime,
                    onDismiss = shareholderEgg.onDismiss,
                )
            }
        },
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = readableGutter(), vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        ) {
            IdentityCard(
                shape = tileShape(1, 0),
                onOpenUrl = actions.onOpenUrl,
                onIconTap = shareholderEgg.onVersionTap,
                onIconPositioned = { iconCenterInRoot = it },
                hint = shareholderEgg.hint,
            )
            Spacer(Modifier.height(SectionGap))
            Group(null, 5) { index, shape ->
                when (index) {
                    0 -> {
                        LinkTile(
                            shape,
                            Icons.Outlined.NewReleases,
                            stringResource(R.string.whats_new),
                            stringResource(R.string.whats_new_summary, BuildConfig.VERSION_NAME),
                            actions.onWhatsNew,
                        )
                    }

                    1 -> {
                        LinkTile(
                            shape,
                            Icons.Outlined.History,
                            stringResource(R.string.changelog),
                            stringResource(R.string.changelog_summary),
                            actions.onChangelog,
                        )
                    }

                    2 -> {
                        LinkTile(
                            shape,
                            ImageVector.vectorResource(R.drawable.ic_github_24),
                            stringResource(R.string.settings_git_repo),
                            REPO,
                            { actions.onOpenUrl(REPO_URL) },
                            external = true,
                            supportingStyle = Meta,
                        )
                    }

                    3 -> {
                        LinkTile(
                            shape,
                            Icons.Outlined.Lightbulb,
                            stringResource(R.string.open_issue),
                            stringResource(R.string.open_issue_summary),
                            { actions.onOpenUrl("$REPO_URL/issues/new/choose") },
                            external = true,
                        )
                    }

                    else -> {
                        LinkTile(
                            shape,
                            Icons.Outlined.Description,
                            stringResource(R.string.licenses),
                            stringResource(R.string.licenses_summary),
                            actions.onLicenses,
                        )
                    }
                }
            }
            Spacer(Modifier.height(SectionGap))
            DeviceGroup(framework)
        }
    }
}

@Stable
private class ShareholderEggState(
    showCelebrationProvider: () -> Boolean,
    hintProvider: () -> String?,
    private val startedAtProvider: () -> Long,
    private val seedProvider: () -> Int,
    private val firstTimeProvider: () -> Boolean,
    val onDismiss: () -> Unit,
    val onVersionTap: () -> Unit,
) {
    val showCelebration: Boolean by derivedStateOf(showCelebrationProvider)
    val hint: String? by derivedStateOf(hintProvider)
    val startedAtNanos: Long get() = startedAtProvider()
    val seed: Int get() = seedProvider()
    val firstTime: Boolean get() = firstTimeProvider()
}

@Composable
private fun rememberShareholderEgg(): ShareholderEggState {
    val context = LocalContext.current
    val alreadyMsg = stringResource(R.string.easter_shareholder_already)
    val countdownLabels =
        (1 until SHAREHOLDER_TAPS - 2).map { n ->
            pluralStringResource(R.plurals.easter_shareholder_countdown, n, n)
        }

    var tapCount by rememberSaveable { mutableIntStateOf(0) }
    var alreadyShareholder by rememberSaveable { mutableStateOf(false) }
    var showCelebration by rememberSaveable { mutableStateOf(false) }
    var startedAtNanos by rememberSaveable { mutableLongStateOf(0L) }
    var seed by rememberSaveable { mutableIntStateOf(0) }
    var firstTime by rememberSaveable { mutableStateOf(false) }
    var hint by rememberSaveable { mutableStateOf<String?>(null) }
    var hintShownAt by remember { mutableLongStateOf(0L) }
    val haptics = LocalHapticFeedback.current
    val prefs = remember(context) { App.from(context).prefsRepository }
    LaunchedEffect(hintShownAt) {
        if (hint == null) return@LaunchedEffect
        delay(HINT_MILLIS)
        hint = null
    }

    fun showHint(text: String) {
        hint = text
        hintShownAt = System.nanoTime()
    }

    return remember {
        ShareholderEggState(
            showCelebrationProvider = { showCelebration },
            hintProvider = { hint },
            startedAtProvider = { startedAtNanos },
            seedProvider = { seed },
            firstTimeProvider = { firstTime },
            onDismiss = { showCelebration = false },
            onVersionTap = onVersionTap@{
                when {
                    showCelebration -> {
                        return@onVersionTap
                    }

                    alreadyShareholder -> {
                        showHint(alreadyMsg)
                    }

                    else -> {
                        tapCount += 1
                        val remaining = SHAREHOLDER_TAPS - tapCount
                        if (remaining == 0) {
                            hint = null
                            tapCount = 0
                            alreadyShareholder = true
                            firstTime = !prefs.read(Prefs.EASTER_EGG_SEEN)
                            prefs.save(Prefs.EASTER_EGG_SEEN, true)
                            startedAtNanos = System.nanoTime()
                            seed = Random.nextInt()
                            showCelebration = true
                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        } else if (remaining in 1..countdownLabels.size) {
                            showHint(countdownLabels[remaining - 1])
                        }
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdentityCard(
    shape: RoundedCornerShape,
    onOpenUrl: (String) -> Unit,
    onIconTap: () -> Unit,
    onIconPositioned: (Offset) -> Unit,
    hint: String?,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = shape, color = scheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val rotation by rememberInfiniteTransition(label = "cookie").animateFloat(
                initialValue = 0f,
                targetValue = FULL_TURN,
                animationSpec = infiniteRepeatable(tween(COOKIE_SPIN_MS, easing = LinearEasing)),
                label = "rotation",
            )
            val cookie = MaterialShapes.Cookie9Sided.toShape()
            Box(
                Modifier
                    .size(CookieSize)
                    .graphicsLayer {
                        rotationZ = rotation
                        this.shape = cookie
                        clip = true
                    }.onGloballyPositioned {
                        onIconPositioned(it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f))
                    }.clickable(onClick = onIconTap),
            ) {
                Image(
                    painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier =
                        Modifier.fillMaxSize().graphicsLayer {
                            rotationZ = -rotation
                            scaleX = ADAPTIVE_ICON_ZOOM
                            scaleY = ADAPTIVE_ICON_ZOOM
                        },
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmallEmphasized)
                AnimatedVisibility(visible = hint != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    var shown by remember { mutableStateOf(hint) }
                    if (hint != null) shown = hint
                    Text(
                        shown.orEmpty(),
                        style = TileSupporting,
                        color = scheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text(
                    stringResource(R.string.app_desc),
                    style = TileSupporting,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { onOpenUrl("$REPO_URL/releases/tag/v${BuildConfig.VERSION_NAME}") },
                    label = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = Meta) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Sell, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    },
                )
                AssistChip(
                    onClick = { onOpenUrl("$REPO_URL/blob/main/LICENSE") },
                    label = { Text(LICENSE, style = Meta) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Balance, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                Button(
                    onClick = { onOpenUrl(REPO_URL) },
                    shapes =
                        ButtonDefaults.shapes(
                            ButtonGroupDefaults.connectedLeadingButtonShape,
                            ButtonGroupDefaults.connectedLeadingButtonPressShape,
                        ),
                    modifier = Modifier.weight(1f).heightIn(min = ConnectedButtonHeight),
                ) {
                    Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.star_on_github), style = ButtonText, maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { onOpenUrl(KOFI_URL) },
                    shapes =
                        ButtonDefaults.shapes(
                            ButtonGroupDefaults.connectedTrailingButtonShape,
                            ButtonGroupDefaults.connectedTrailingButtonPressShape,
                        ),
                    modifier = Modifier.weight(1f).heightIn(min = ConnectedButtonHeight),
                ) {
                    Icon(Icons.Outlined.Coffee, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.kofi), style = ButtonText, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun LinkTile(
    shape: RoundedCornerShape,
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
    external: Boolean = false,
    supportingStyle: TextStyle = TileSupporting,
) {
    Tile(shape = shape, onClick = onClick) {
        TileLeading(icon)
        TileText(title, summary, supportingStyle = supportingStyle)
        Box(Modifier.width(ChevronSlot), contentAlignment = Alignment.Center) {
            Icon(
                if (external) Icons.AutoMirrored.Outlined.OpenInNew else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeviceGroup(framework: String?) {
    var showDiagnostics by rememberSaveable { mutableStateOf(false) }
    if (showDiagnostics) {
        DiagnosticsDialog(frameworkVersion = framework, onDismiss = { showDiagnostics = false })
    }
    val tiles = if (framework == null) 1 else 2
    Group(stringResource(R.string.settings_this_device), tiles) { index, shape ->
        if (index == 0 && framework != null) {
            Tile(shape = shape) {
                TileLeading(Icons.Outlined.Extension)
                TileText(stringResource(R.string.settings_framework), framework.replace("\n", " \u00B7 "), supportingStyle = Meta)
            }
        } else {
            LinkTile(
                shape = shape,
                icon = Icons.Outlined.Troubleshoot,
                title = stringResource(R.string.settings_diagnostics),
                summary = stringResource(R.string.settings_diagnostics_summary),
                onClick = { showDiagnostics = true },
            )
        }
    }
}

@Composable
private fun Group(
    title: String?,
    count: Int,
    content: @Composable (Int, RoundedCornerShape) -> Unit,
) {
    if (title != null) {
        Text(
            title,
            style = Subheader,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp).semantics { heading() },
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(TileGap)) {
        repeat(count) { content(it, tileShape(count, it)) }
    }
}

private fun tileShape(
    count: Int,
    index: Int,
): RoundedCornerShape =
    when {
        count == 1 -> RoundedCornerShape(TileOuterRadius)
        index == 0 -> RoundedCornerShape(TileOuterRadius, TileOuterRadius, TileInnerRadius, TileInnerRadius)
        index == count - 1 -> RoundedCornerShape(TileInnerRadius, TileInnerRadius, TileOuterRadius, TileOuterRadius)
        else -> RoundedCornerShape(TileInnerRadius)
    }

@Composable
private fun Tile(
    shape: RoundedCornerShape,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val row: @Composable () -> Unit = {
        Row(
            modifier =
                Modifier
                    .heightIn(min = TileMinHeight)
                    .padding(start = TileStartPadding, end = TileEndPadding, top = TileVerticalPadding, bottom = TileVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(TileSlotSpacing),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
    val color = MaterialTheme.colorScheme.surfaceContainer
    if (onClick != null) {
        Surface(onClick = onClick, shape = shape, color = color, modifier = Modifier.fillMaxWidth()) { row() }
    } else {
        Surface(shape = shape, color = color, modifier = Modifier.fillMaxWidth()) { row() }
    }
}

@Composable
private fun TileLeading(symbol: ImageVector) {
    Icon(symbol, contentDescription = null, modifier = Modifier.size(RowGlyphSize), tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun RowScope.TileText(
    title: String,
    supporting: String? = null,
    supportingStyle: TextStyle = TileSupporting,
) {
    Column(Modifier.weight(1f)) {
        Text(title, style = TileTitle, color = MaterialTheme.colorScheme.onSurface)
        if (supporting != null) {
            Text(supporting, style = supportingStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun compactHeight(): Boolean =
    with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height
            .toDp()
    } < CompactHeight

@Composable
private fun readableGutter(): Dp {
    val windowWidth =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
        }
    return max(MinGutter, (windowWidth - ContentMaxWidth) / 2)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    overlay: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Box {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            topBar = {
                val navigationIcon: @Composable () -> Unit = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
                if (compactHeight()) {
                    TopAppBar(title = { Text(title) }, navigationIcon = navigationIcon, scrollBehavior = scrollBehavior)
                } else {
                    LargeFlexibleTopAppBar(title = { Text(title) }, navigationIcon = navigationIcon, scrollBehavior = scrollBehavior)
                }
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
            ) { content() }
        }
        overlay()
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    PreviewWrapper {
        AboutScreen("LSPosed 2.2.1\nAPI 102", AboutActions({}, {}, {}, {}), onBack = {})
    }
}
