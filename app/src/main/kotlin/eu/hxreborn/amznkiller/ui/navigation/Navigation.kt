package eu.hxreborn.amznkiller.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.dashboard.DashboardScreen
import eu.hxreborn.amznkiller.ui.screen.settings.LicensesScreen
import eu.hxreborn.amznkiller.ui.screen.settings.SettingsScreen
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
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

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
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
                    )
                }
                entry<Screen.Settings> {
                    SettingsScreen(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        onNavigateToLicenses = { backStack.add(Screen.Licenses) },
                    )
                }
                entry<Screen.Licenses> {
                    LicensesScreen(onBack = { backStack.removeLastOrNull() })
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
) {
    val haptics = LocalHapticFeedback.current
    val buttonBounds = remember { mutableStateMapOf<Int, Rect>() }
    val currentIndex = bottomNavItems.indexOfFirst { it.key == currentKey }.coerceAtLeast(0)
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
    val pillColor = MaterialTheme.colorScheme.primary

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = Tokens.FloatingBarBottomPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors =
                FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                    toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                bottomNavItems.forEachIndexed { index, item ->
                    val selected = currentKey == item.key

                    ToggleButton(
                        checked = selected,
                        onCheckedChange = {
                            if (!selected) {
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
                            ToggleButtonDefaults.toggleButtonColors(
                                containerColor = Color.Transparent,
                                checkedContainerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        shapes =
                            ToggleButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = CircleShape,
                                checkedShape = CircleShape,
                            ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = stringResource(item.titleRes),
                            )
                            AnimatedVisibility(
                                visible = selected,
                                enter =
                                    expandHorizontally(
                                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                    ),
                                exit =
                                    shrinkHorizontally(
                                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                    ),
                            ) {
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
