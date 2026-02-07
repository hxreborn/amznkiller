package eu.hxreborn.amznkiller.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.rememberNavBackStack
import eu.hxreborn.amznkiller.ui.navigation.BottomNav
import eu.hxreborn.amznkiller.ui.navigation.MainNavDisplay
import eu.hxreborn.amznkiller.ui.navigation.Screen
import eu.hxreborn.amznkiller.ui.screen.dashboard.FilterViewModel

@Composable
fun MainScaffold(viewModel: FilterViewModel) {
    val backStack = rememberNavBackStack(Screen.Dashboard)
    val currentKey = backStack.lastOrNull() as? Screen

    Scaffold(
        bottomBar = {
            BottomNav(
                backStack = backStack,
                currentKey = currentKey,
            )
        },
    ) { contentPadding ->
        MainNavDisplay(
            backStack = backStack,
            viewModel = viewModel,
            contentPadding = contentPadding,
        )
    }
}
