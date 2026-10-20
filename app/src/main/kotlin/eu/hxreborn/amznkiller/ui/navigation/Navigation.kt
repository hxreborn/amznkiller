package eu.hxreborn.amznkiller.ui.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.amznkiller.App
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.about.AboutActions
import eu.hxreborn.amznkiller.ui.screen.about.AboutScreen
import eu.hxreborn.amznkiller.ui.screen.dashboard.DashboardScreen
import eu.hxreborn.amznkiller.ui.screen.settings.LicensesScreen
import eu.hxreborn.amznkiller.ui.screen.settings.SettingsScreen
import eu.hxreborn.amznkiller.ui.screen.update.ChangelogScreen
import eu.hxreborn.amznkiller.ui.screen.update.UpdateActions
import eu.hxreborn.amznkiller.ui.screen.update.UpdateCard
import eu.hxreborn.amznkiller.ui.screen.update.UpdateSettingsActions
import eu.hxreborn.amznkiller.ui.screen.update.UpdateSheet
import eu.hxreborn.amznkiller.ui.screen.update.WhatsNewSheet
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import eu.hxreborn.amznkiller.ui.viewmodel.UpdateViewModel
import kotlinx.serialization.Serializable
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut

sealed interface Screen : NavKey {
    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Licenses : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object Update : Screen

    @Serializable
    data object WhatsNew : Screen

    @Serializable
    data object Changelog : Screen
}

data class BottomNavItem(
    val key: Screen,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private fun tabIndexOf(contentKey: Any?): Int = bottomNavItems.indexOfFirst { it.key.toString() == contentKey }

private fun isForwardTabTransition(
    initial: Scene<NavKey>,
    target: Scene<NavKey>,
): Boolean {
    val initialIdx = tabIndexOf(initial.entries.lastOrNull()?.contentKey)
    val targetIdx = tabIndexOf(target.entries.lastOrNull()?.contentKey)
    return if (initialIdx >= 0 && targetIdx >= 0) targetIdx >= initialIdx else true
}

val bottomNavItems =
    listOf(
        BottomNavItem(
            key = Screen.Dashboard,
            titleRes = R.string.tab_dashboard,
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        BottomNavItem(
            key = Screen.Settings,
            titleRes = R.string.tab_settings,
            selectedIcon = Icons.Rounded.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
    )

@Composable
fun MainNavDisplay(
    backStack: NavBackStack<NavKey>,
    viewModel: AppViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val slideInDistance = with(LocalDensity.current) { Tokens.NavSlideDistance.roundToPx() }
    val updates: UpdateViewModel = viewModel()
    val updateState by updates.state.collectAsStateWithLifecycle()
    val changelog by updates.changelog.collectAsStateWithLifecycle()
    val sheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        sceneStrategies = listOf(sheetStrategy),
        transitionSpec = {
            val forward = isForwardTabTransition(initialState, targetState)
            materialSharedAxisXIn(forward = forward, slideDistance = slideInDistance) togetherWith
                materialSharedAxisXOut(
                    forward = forward,
                    slideDistance = slideInDistance,
                )
        },
        popTransitionSpec = {
            materialSharedAxisXIn(forward = false, slideDistance = slideInDistance) togetherWith
                materialSharedAxisXOut(
                    forward = false,
                    slideDistance = slideInDistance,
                )
        },
        predictivePopTransitionSpec = {
            materialSharedAxisXIn(forward = false, slideDistance = slideInDistance) togetherWith
                materialSharedAxisXOut(
                    forward = false,
                    slideDistance = slideInDistance,
                )
        },
        entryProvider =
            entryProvider {
                entry<Screen.Dashboard> {
                    DashboardScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        updateCard = {
                            val release = updateState.available
                            if (release != null && updateState.showUpdateCard) {
                                UpdateCard(
                                    release = release,
                                    onLater = { updates.postponeUpdate(release.versionCode) },
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    onUpdate = {
                                        updates.resetInstall()
                                        backStack.add(Screen.Update)
                                    },
                                )
                            }
                        },
                    )
                }
                entry<Screen.Settings> {
                    SettingsScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onNavigateToAbout = { backStack.add(Screen.About) },
                        updateState = updateState,
                        updateActions =
                            UpdateSettingsActions(
                                onCheck = { updates.checkRelease(force = true) },
                                onUpdate = {
                                    updates.resetInstall()
                                    backStack.add(Screen.Update)
                                },
                                onAutoCheck = updates::setAutoCheckUpdates,
                            ),
                    )
                }
                entry<Screen.About> {
                    val xposed by App.from(context).xposedState.collectAsStateWithLifecycle()
                    AboutScreen(
                        framework = xposed.frameworkLabel,
                        actions =
                            AboutActions(
                                onWhatsNew = { backStack.add(Screen.WhatsNew) },
                                onChangelog = { backStack.add(Screen.Changelog) },
                                onLicenses = { backStack.add(Screen.Licenses) },
                                onOpenUrl = uriHandler::openUri,
                            ),
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<Screen.Licenses> {
                    LicensesScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<Screen.WhatsNew>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                    LaunchedEffect(Unit) { updates.loadChangelog() }
                    WhatsNewSheet(
                        state = changelog,
                        onFullChangelog = {
                            backStack.remove(Screen.WhatsNew)
                            backStack.add(Screen.Changelog)
                        },
                        onClose = { backStack.remove(Screen.WhatsNew) },
                    )
                }
                entry<Screen.Changelog> {
                    LaunchedEffect(Unit) { updates.loadChangelog() }
                    ChangelogScreen(
                        state = changelog,
                        onRetry = updates::loadChangelog,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<Screen.Update>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                    val release = updateState.available
                    if (release == null) {
                        LaunchedEffect(Unit) { backStack.remove(Screen.Update) }
                    } else {
                        LifecycleResumeEffect(release) {
                            updates.resumeAfterInstallPermission()
                            onPauseOrDispose {}
                        }
                        UpdateSheet(
                            release = release,
                            step = updateState.install,
                            actions =
                                UpdateActions(
                                    onInstall = { updates.startInstall(release) },
                                    onInstallAnyway = { updates.installAnyway(release) },
                                    onOpenSettings = {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                "package:${context.packageName}".toUri(),
                                            ),
                                        )
                                    },
                                    onGitHub = { uriHandler.openUri(release.url) },
                                    onCancel = updates::cancelInstall,
                                ),
                        )
                    }
                }
            },
    )
}

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)
@Composable
fun BottomNav(
    backStack: NavBackStack<NavKey>,
    currentKey: NavKey?,
    modifier: Modifier = Modifier,
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
) {
    val haptics = LocalHapticFeedback.current
    val buttonBounds = remember { mutableStateMapOf<Int, Rect>() }

    val highlightKey: NavKey? = backStack.lastOrNull { key -> bottomNavItems.any { it.key == key } }
    val currentIndex = bottomNavItems.indexOfFirst { it.key == highlightKey }.coerceAtLeast(0)
    val targetRect = buttonBounds[currentIndex]
    val anchorRect = buttonBounds[0]
    val pillTargetX = (targetRect?.left ?: 0f) - (anchorRect?.left ?: 0f)
    val pillTargetWidth = targetRect?.width ?: 0f

    val pillAnimatedX by animateFloatAsState(
        targetValue = pillTargetX,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "navPillX",
    )
    val pillAnimatedWidth by animateFloatAsState(
        targetValue = pillTargetWidth,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "navPillWidth",
    )
    val pillColor = MaterialTheme.colorScheme.secondaryContainer

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        scrollBehavior = scrollBehavior,
        colors =
            FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            bottomNavItems.forEachIndexed { index, item ->
                val selected = highlightKey == item.key

                ToggleButton(
                    checked = selected,
                    onCheckedChange = {
                        if (currentKey != item.key) {
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            backStack.clear()
                            backStack.add(item.key)
                        }
                    },
                    modifier =
                        Modifier
                            .height(Tokens.FloatingBarItemHeight)
                            .onGloballyPositioned { coords ->
                                buttonBounds[index] = coords.boundsInParent()
                            }.then(
                                if (index == 0) {
                                    Modifier.drawWithContent {
                                        if (pillAnimatedWidth > 0f) {
                                            drawRoundRect(
                                                color = pillColor,
                                                topLeft = Offset(pillAnimatedX, 0f),
                                                size = Size(pillAnimatedWidth, size.height),
                                                cornerRadius = CornerRadius(size.height / 2f),
                                            )
                                        }
                                        drawContent()
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                    colors =
                        ToggleButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            checkedContainerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    shapes =
                        ToggleButtonShapes(
                            shape = CircleShape,
                            pressedShape = CircleShape,
                            checkedShape = CircleShape,
                        ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(item.titleRes),
                            modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomNavPreview() {
    PreviewWrapper {
        val backStack = rememberNavBackStack(Screen.Dashboard)
        BottomNav(
            backStack = backStack,
            currentKey = Screen.Dashboard,
        )
    }
}
