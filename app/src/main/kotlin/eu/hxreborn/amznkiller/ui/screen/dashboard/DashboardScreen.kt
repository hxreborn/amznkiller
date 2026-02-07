package eu.hxreborn.amznkiller.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.animation.AnimationState
import eu.hxreborn.amznkiller.ui.animation.BUTTON_UNLOCK_THRESHOLD
import eu.hxreborn.amznkiller.ui.animation.rememberFillLevelState
import eu.hxreborn.amznkiller.ui.component.AboutCard
import eu.hxreborn.amznkiller.ui.component.ControlCard
import eu.hxreborn.amznkiller.ui.component.RulesBottomSheet
import eu.hxreborn.amznkiller.ui.component.StatusCard
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.FilterPrefsState
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import eu.hxreborn.amznkiller.ui.theme.Tokens
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val AMAZON_PACKAGE = "com.amazon.mShop.android.shopping"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FilterViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRulesSheet by remember { mutableStateOf(false) }
    var pendingEvent by remember { mutableStateOf<UpdateEvent?>(null) }
    var buttonLocked by remember { mutableStateOf(false) }
    val tagline =
        rememberSaveable {
            context.resources.getStringArray(R.array.taglines).random()
        }

    LaunchedEffect(Unit) {
        viewModel.updateEvents.collect { event ->
            pendingEvent = event
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isExpandedSlot = LocalTextStyle.current.fontSize >= MaterialTheme.typography.headlineMedium.fontSize
                    Column {
                        Text(
                            text = stringResource(R.string.app_bar_title),
                            style =
                                if (isExpandedSlot) {
                                    MaterialTheme.typography.headlineLarge.copy(
                                        lineHeight = Tokens.ExpandedTitleLineHeight,
                                    )
                                } else {
                                    LocalTextStyle.current
                                },
                            maxLines = if (isExpandedSlot) Tokens.ExpandedTitleMaxLines else 1,
                        )
                        Text(
                            text = tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 1f - scrollBehavior.state.collapsedFraction,
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val mergedPadding =
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            )
        when (val state = uiState) {
            is FilterUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(mergedPadding),
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
                        onStateChange = { animState ->
                            when (animState) {
                                is AnimationState.Filling -> {
                                    buttonLocked = true
                                }

                                is AnimationState.Finishing -> {
                                    if (animState.value >= BUTTON_UNLOCK_THRESHOLD) {
                                        buttonLocked = false
                                        pendingEvent?.let { event ->
                                            val message = formatUpdateEventMessage(context, event)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                            pendingEvent = null
                                        }
                                    }
                                }

                                is AnimationState.Completed -> {
                                    buttonLocked = false
                                    pendingEvent?.let { event ->
                                        val message = formatUpdateEventMessage(context, event)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                        pendingEvent = null
                                    }
                                }

                                is AnimationState.Idle -> {}
                            }
                        },
                    )

                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(mergedPadding),
                ) {
                    val minContentHeight = maxHeight + 1.dp
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = minContentHeight),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            StatusCard(
                                isActive = prefs.isXposedActive,
                                fillState = fillState,
                                selectorCount = prefs.selectorCount,
                                lastFetched = prefs.lastFetched,
                                onShowRules = { showRulesSheet = true },
                            )

                            ControlCard(
                                isRefreshing = buttonLocked || prefs.isRefreshing,
                                onUpdate = { viewModel.refreshAll() },
                                onOpenAmazon = {
                                    val intent = context.packageManager.getLaunchIntentForPackage(AMAZON_PACKAGE)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    } else {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                },
                            )

                            AboutCard()
                        }
                    }
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

@Suppress("ViewModelConstructorInComposable")
@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    PreviewWrapper {
        DashboardScreen(viewModel = PreviewFilterViewModel())
    }
}

private class PreviewFilterViewModel : FilterViewModel() {
    private val _uiState =
        MutableStateFlow(
            FilterUiState.Success(
                prefs =
                    FilterPrefsState(
                        isXposedActive = true,
                        isRefreshing = false,
                        isRefreshFailed = false,
                        selectorCount = 42,
                        lastFetched = System.currentTimeMillis() - 3_600_000,
                        cachedSelectors =
                            listOf(
                                ".s-sponsored-label",
                                ".a-section.a-spacing-none",
                                "[data-component-type=\"sp\"]",
                            ),
                        injectionEnabled = true,
                    ),
            ),
        )
    override val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()
    private val _updateEvents = MutableSharedFlow<UpdateEvent>()
    override val updateEvents: SharedFlow<UpdateEvent> = _updateEvents

    override fun refreshAll() {
    }

    override fun setXposedActive(active: Boolean) {
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
    }
}
