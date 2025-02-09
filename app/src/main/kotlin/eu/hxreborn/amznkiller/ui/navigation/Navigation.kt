package eu.hxreborn.amznkiller.ui.navigation

import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
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

private val SLIDE_DISTANCE = 96.dp

@Composable
fun MainNavDisplay(
    backStack: NavBackStack<NavKey>,
    viewModel: AppViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val slideInDistance = with(LocalDensity.current) { SLIDE_DISTANCE.roundToPx() }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        transitionSpec = {
            materialSharedAxisXIn(forward = true, slideDistance = slideInDistance) togetherWith
                materialSharedAxisXOut(
                    forward = true,
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

@Composable
fun BottomNav(
    backStack: NavBackStack<NavKey>,
    currentKey: NavKey?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val animDuration =
        remember {
            val scale =
                runCatching {
                    Settings.Global.getFloat(
                        context.contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE,
                        1f,
                    )
                }.getOrDefault(1f)
            (Tokens.ANIMATION_DURATION_MS * scale).toInt().coerceAtLeast(0)
        }

    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentKey == item.key
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        backStack.removeAll { it != Screen.Dashboard }
                        if (item.key != Screen.Dashboard) {
                            backStack.add(item.key)
                        }
                    }
                },
                icon = {
                    Crossfade(
                        targetState = selected,
                        animationSpec = tween(durationMillis = animDuration),
                        label = "iconCrossfade",
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = stringResource(item.titleRes),
                        )
                    }
                },
                label = { Text(stringResource(item.titleRes)) },
                alwaysShowLabel = false,
            )
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
