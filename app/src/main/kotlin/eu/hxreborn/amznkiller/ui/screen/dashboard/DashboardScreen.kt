package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Intent
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.ui.component.RulesBottomSheet
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.FilterPrefsState
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.relativeTime
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals

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
    var showRulesSheet by remember { mutableStateOf(false) }
    var lastHandledOutcomeId by remember { mutableStateOf<Long?>(null) }
    val tagline =
        rememberSaveable {
            context.resources.getStringArray(R.array.taglines).random()
        }

    val amazonInfo =
        remember {
            runCatching {
                val pm = context.packageManager
                val info = pm.getPackageInfo(AMAZON_PACKAGE, 0)
                val icon = pm.getApplicationIcon(AMAZON_PACKAGE).toBitmap(128, 128).asImageBitmap()
                val label = pm.getApplicationLabel(info.applicationInfo!!).toString()
                Triple(icon, label, info.versionName)
            }.getOrNull()
        }

    val webViewInfo =
        remember {
            runCatching {
                val pkg = WebView.getCurrentWebViewPackage() ?: return@runCatching null
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(pkg.packageName, 0)
                val icon = pm.getApplicationIcon(appInfo).toBitmap(128, 128).asImageBitmap()
                val label = pm.getApplicationLabel(appInfo).toString()
                Triple(icon, label, pkg.versionName)
            }.getOrNull()
        }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isExpandedSlot =
                        LocalTextStyle.current.fontSize >=
                            MaterialTheme.typography.headlineMedium.fontSize
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
        when (val state = uiState) {
            is FilterUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is FilterUiState.Success -> {
                val prefs = state.prefs

                val outcome = prefs.lastRefreshOutcome
                LaunchedEffect(outcome?.id) {
                    if (outcome != null && outcome.id != lastHandledOutcomeId) {
                        lastHandledOutcomeId = outcome.id
                        val message = formatUpdateEventMessage(context, outcome.event)
                        snackbarHostState.showSnackbar(message)
                    }
                }

                val surface = MaterialTheme.colorScheme.surfaceVariant

                ProvidePreferenceLocals {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                                    start = 8.dp,
                                    end = 8.dp,
                                ),
                    ) {
                        // Target
                        PreferenceCategory(
                            title = { Text(stringResource(R.string.dashboard_target)) },
                        )

                        val targetShape = shapeForPosition(1, 0)
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        color = surface,
                                        shape = targetShape,
                                    ).clip(targetShape)
                                    .padding(16.dp),
                        ) {
                            TargetRow(amazonInfo) {
                                if (amazonInfo != null) {
                                    FilledTonalButton(
                                        onClick = {
                                            context.packageManager
                                                .getLaunchIntentForPackage(AMAZON_PACKAGE)
                                                ?.let {
                                                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(it)
                                                }
                                        },
                                        contentPadding = COMPACT_BUTTON_PADDING,
                                        modifier = Modifier.height(COMPACT_BUTTON_HEIGHT),
                                    ) {
                                        Text(
                                            stringResource(R.string.dashboard_launch),
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            TargetRow(webViewInfo, isGrayscale = true)
                        }

                        // Injection Status
                        PreferenceCategory(
                            title = { Text(stringResource(R.string.dashboard_status)) },
                        )

                        val statusCount = 3
                        val xposedShape = shapeForPosition(statusCount, 0)
                        Preference(
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(color = surface, shape = xposedShape)
                                    .clip(xposedShape),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Extension,
                                    contentDescription = null,
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.env_xposed),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            summary = {
                                Text(
                                    text =
                                        if (prefs.isXposedActive) {
                                            stringResource(R.string.env_xposed_connected)
                                        } else {
                                            stringResource(R.string.env_xposed_disconnected)
                                        },
                                    color =
                                        if (prefs.isXposedActive) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                )
                            },
                        )

                        Spacer(Modifier.height(2.dp))

                        val methodShape = shapeForPosition(statusCount, 1)
                        Preference(
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(color = surface, shape = methodShape)
                                    .clip(methodShape),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Vaccines,
                                    contentDescription = null,
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.dashboard_method),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            summary = {
                                Text(
                                    text = stringResource(R.string.dashboard_css_injection),
                                )
                            },
                        )

                        Spacer(Modifier.height(2.dp))

                        val selectorsShape = shapeForPosition(statusCount, 2)
                        Preference(
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(color = surface, shape = selectorsShape)
                                    .clip(selectorsShape),
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Rule,
                                    contentDescription = null,
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.config_active_selectors),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            summary = {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.dashboard_selectors_applied,
                                            prefs.selectorCount,
                                        ),
                                )
                            },
                            widgetContainer = {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                            },
                            onClick = { showRulesSheet = true },
                        )

                        // Updates
                        PreferenceCategory(
                            title = { Text(stringResource(R.string.dashboard_updates)) },
                        )

                        val outcomeEvent = prefs.lastRefreshOutcome?.event
                        val updatesShape = shapeForPosition(1, 0)
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        color = surface,
                                        shape = updatesShape,
                                    ).clip(updatesShape)
                                    .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    prefs.isRefreshing -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }

                                    outcomeEvent is UpdateEvent.UpToDate ||
                                        outcomeEvent is UpdateEvent.Updated -> {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    outcomeEvent is UpdateEvent.Error -> {
                                        Icon(
                                            imageVector = Icons.Outlined.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }

                                    else -> {
                                        Icon(
                                            imageVector = Icons.Outlined.Sync,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.dashboard_rule_definitions),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text =
                                        when {
                                            prefs.isRefreshing -> {
                                                stringResource(R.string.dashboard_checking)
                                            }

                                            outcomeEvent is UpdateEvent.UpToDate -> {
                                                stringResource(R.string.dashboard_up_to_date)
                                            }

                                            outcomeEvent is UpdateEvent.Updated -> {
                                                stringResource(
                                                    R.string.dashboard_rules_updated,
                                                    outcomeEvent.added + outcomeEvent.removed,
                                                )
                                            }

                                            outcomeEvent is UpdateEvent.Error -> {
                                                outcomeEvent.message
                                            }

                                            else -> {
                                                relativeTime(prefs.lastFetched)
                                            }
                                        },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                        when (outcomeEvent) {
                                            is UpdateEvent.Error -> {
                                                MaterialTheme.colorScheme.error
                                            }

                                            else -> {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        },
                                )
                            }
                            FilledTonalButton(
                                onClick = { viewModel.refreshAll() },
                                enabled = !prefs.isRefreshing,
                                contentPadding = COMPACT_BUTTON_PADDING,
                                modifier = Modifier.height(COMPACT_BUTTON_HEIGHT),
                            ) {
                                Text(
                                    stringResource(R.string.dashboard_check),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
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

private val COMPACT_BUTTON_HEIGHT = 32.dp
private val COMPACT_BUTTON_PADDING = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
private val GRAYSCALE = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
private fun TargetRow(
    info: Triple<ImageBitmap, String, String?>?,
    isGrayscale: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (info != null) {
            Image(
                bitmap = info.first,
                contentDescription = null,
                colorFilter = if (isGrayscale) GRAYSCALE else null,
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp)),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    info?.second
                        ?: stringResource(R.string.target_title),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    if (info != null) {
                        "v${info.third}"
                    } else {
                        stringResource(R.string.dashboard_not_installed)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (info == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
        trailing()
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

    override fun refreshAll() {}

    override fun setXposedActive(active: Boolean) {}

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {}
}
