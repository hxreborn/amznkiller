package eu.hxreborn.amznkiller.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import eu.hxreborn.amznkiller.ui.navigation.BottomNav
import eu.hxreborn.amznkiller.ui.navigation.MainNavDisplay
import eu.hxreborn.amznkiller.ui.navigation.Screen
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel

@Composable
fun MainScaffold(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(Screen.Dashboard)
    val currentKey = backStack.lastOrNull() as? Screen

    Scaffold(
        modifier = modifier,
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
