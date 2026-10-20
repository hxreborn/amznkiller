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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.navigation.BottomNav
import eu.hxreborn.amznkiller.ui.navigation.MainNavDisplay
import eu.hxreborn.amznkiller.ui.navigation.Screen
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import eu.hxreborn.amznkiller.ui.viewmodel.UpdateViewModel
import kotlinx.coroutines.launch

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
    val updates: UpdateViewModel = viewModel()
    val updatedNotice by updates.updatedNotice.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val updatedText = stringResource(R.string.updated_to, BuildConfig.VERSION_NAME)
    val noticeScope = rememberCoroutineScope()
    LaunchedEffect(updatedNotice) {
        if (!updatedNotice) return@LaunchedEffect
        updates.consumeUpdatedNotice()
        noticeScope.launch { snackbar.showSnackbar(updatedText, duration = SnackbarDuration.Short) }
    }
    LifecycleResumeEffect(Unit) {
        updates.checkRelease(force = false)
        onPauseOrDispose {}
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior),
        snackbarHost = { SnackbarHost(snackbar) },
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
