package eu.hxreborn.amznkiller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation3.runtime.rememberNavBackStack
import eu.hxreborn.amznkiller.ui.navigation.BottomNav
import eu.hxreborn.amznkiller.ui.navigation.MainNavDisplay
import eu.hxreborn.amznkiller.ui.navigation.Screen
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScaffold(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(Screen.Dashboard)
    val currentKey = backStack.lastOrNull() as? Screen
    val scrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            exitDirection = FloatingToolbarExitDirection.Bottom,
        )
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior),
        bottomBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                BottomNav(
                    backStack = backStack,
                    currentKey = currentKey,
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.padding(bottom = navBarBottom + Tokens.FloatingBarBottomPadding),
                )
            }
        },
    ) { contentPadding ->
        MainNavDisplay(
            backStack = backStack,
            viewModel = viewModel,
            contentPadding = contentPadding,
        )
    }
}
