package eu.hxreborn.amznkiller.ui.screen

import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.FilterUiState
import eu.hxreborn.amznkiller.ui.FilterViewModel
import eu.hxreborn.amznkiller.ui.animation.AnimationState
import eu.hxreborn.amznkiller.ui.animation.rememberFillLevelState
import eu.hxreborn.amznkiller.ui.component.AboutCard
import eu.hxreborn.amznkiller.ui.component.ControlCard
import eu.hxreborn.amznkiller.ui.component.RulesBottomSheet
import eu.hxreborn.amznkiller.ui.component.StatusCard
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import kotlinx.coroutines.launch

private const val AMAZON_PACKAGE = "com.amazon.mShop.android.shopping"

private val taglines =
    listOf(
        R.string.tagline_1,
        R.string.tagline_2,
        R.string.tagline_3,
        R.string.tagline_4,
        R.string.tagline_5,
        R.string.tagline_6,
        R.string.tagline_7,
        R.string.tagline_8,
        R.string.tagline_9,
        R.string.tagline_10,
        R.string.tagline_11,
        R.string.tagline_12,
        R.string.tagline_13,
        R.string.tagline_14,
        R.string.tagline_15,
        R.string.tagline_16,
        R.string.tagline_17,
        R.string.tagline_18,
        R.string.tagline_19,
        R.string.tagline_20,
        R.string.tagline_21,
        R.string.tagline_22,
        R.string.tagline_23,
        R.string.tagline_24,
        R.string.tagline_25,
        R.string.tagline_26,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterListScreen(viewModel: FilterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRulesSheet by remember { mutableStateOf(false) }
    var pendingEvent by remember { mutableStateOf<UpdateEvent?>(null) }
    val taglineRes = rememberSaveable { taglines.random() }

    LaunchedEffect(Unit) {
        viewModel.updateEvents.collect { event ->
            pendingEvent = event
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.app_bar_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val state = uiState) {
            is FilterUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is FilterUiState.Success -> {
                val prefs = state.prefs
                val fillState =
                    rememberFillLevelState(
                        isRefreshing = prefs.isRefreshing,
                        isError = prefs.isRefreshFailed,
                        onStateChange = { state ->
                            if (state is AnimationState.Completed) {
                                pendingEvent?.let { event ->
                                    val message = formatUpdateEventMessage(context, event)
                                    if (event is UpdateEvent.Error) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                    pendingEvent = null
                                }
                            }
                        },
                    )

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(taglineRes),
                        style =
                            androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color =
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                    )

                    StatusCard(
                        isActive = prefs.isXposedActive,
                        fillState = fillState,
                        selectorCount = prefs.selectorCount,
                        lastFetched = prefs.lastFetched,
                        onShowRules = { showRulesSheet = true },
                    )

                    Spacer(Modifier.height(12.dp))

                    ControlCard(
                        isRefreshing = prefs.isRefreshing,
                        onUpdate = { viewModel.refreshAll() },
                        onOpenAmazon = {
                            val intent =
                                context.packageManager
                                    .getLaunchIntentForPackage(AMAZON_PACKAGE)
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        },
                    )

                    Spacer(Modifier.height(12.dp))

                    AboutCard()

                    Spacer(Modifier.height(100.dp))
                }

                if (showRulesSheet) {
                    RulesBottomSheet(
                        selectors = prefs.cachedSelectors,
                        onDismiss = { showRulesSheet = false },
                    )
                }
            }
        }
    }
}

private fun formatUpdateEventMessage(
    context: android.content.Context,
    event: UpdateEvent,
): String =
    when (event) {
        is UpdateEvent.Updated -> {
            val total = event.added + event.removed
            context.getString(R.string.snackbar_updated, total.toString())
        }

        is UpdateEvent.UpToDate -> {
            context.getString(R.string.snackbar_up_to_date)
        }

        is UpdateEvent.Error -> {
            event.message
        }
    }
