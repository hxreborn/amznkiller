package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.Intent
import android.os.Build
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
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
import eu.hxreborn.amznkiller.ui.state.AppPrefsState
import eu.hxreborn.amznkiller.ui.state.SelectorSyncEvent
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.relativeTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val AMAZON_PACKAGE = "com.amazon.mShop.android.shopping"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
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
                            maxLines =
                                if (isExpandedSlot) {
                                    Tokens.ExpandedTitleMaxLines
                                } else {
                                    1
                                },
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
            is AppUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is AppUiState.Success -> {
                val prefs = state.prefs

                val outcome = prefs.lastRefreshOutcome
                LaunchedEffect(outcome?.id) {
                    if (outcome != null && outcome.id != lastHandledOutcomeId) {
                        lastHandledOutcomeId = outcome.id
                        val message =
                            formatUpdateEventMessage(
                                context,
                                outcome.event,
                            )
                        snackbarHostState.showSnackbar(message)
                    }
                }

                val surface = MaterialTheme.colorScheme.surfaceVariant

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    contentPadding =
                        PaddingValues(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = contentPadding.calculateBottomPadding() + 16.dp,
                        ),
                ) {
                    item {
                        UpdatesCard(
                            prefs = prefs,
                            surface = surface,
                            onRefresh = { viewModel.refreshAll() },
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        val shape = Tokens.CardShape
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        color = surface,
                                        shape = shape,
                                    ).clip(shape)
                                    .padding(16.dp),
                        ) {
                            TargetRow(amazonInfo) {
                                if (amazonInfo != null) {
                                    IconButton(
                                        onClick = {
                                            context.packageManager
                                                .getLaunchIntentForPackage(
                                                    AMAZON_PACKAGE,
                                                )?.let {
                                                    it.addFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK,
                                                    )
                                                    context.startActivity(
                                                        it,
                                                    )
                                                }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                            contentDescription =
                                                stringResource(
                                                    R.string.dashboard_launch,
                                                ),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        MetricsGrid(
                            prefs = prefs,
                            surface = surface,
                            onShowRules = { showRulesSheet = true },
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        SystemEnvironmentCard(
                            prefs = prefs,
                            surface = surface,
                        )
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

@Composable
private fun UpdatesCard(
    prefs: AppPrefsState,
    surface: Color,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outcomeEvent = prefs.lastRefreshOutcome?.event
    val isError = outcomeEvent is SelectorSyncEvent.Error
    val isUpToDate = !prefs.isStale && !isError && prefs.lastFetched > 0L

    val shape = Tokens.CardShape
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = surface, shape = shape)
                .clip(shape)
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

                isError -> {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                isUpToDate -> {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    when {
                        prefs.isRefreshing -> {
                            stringResource(R.string.hero_checking_title)
                        }

                        isError -> {
                            stringResource(R.string.hero_error_title)
                        }

                        isUpToDate -> {
                            stringResource(
                                R.string.hero_operational_title,
                            )
                        }

                        else -> {
                            stringResource(R.string.hero_stale_title)
                        }
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    when {
                        prefs.isRefreshing -> {
                            stringResource(
                                R.string.hero_checking_subtitle,
                            )
                        }

                        isError -> {
                            outcomeEvent.message
                        }

                        isUpToDate -> {
                            buildString {
                                append(
                                    stringResource(
                                        R.string.hero_operational_subtitle,
                                    ),
                                )
                                append("\n")
                                append(
                                    stringResource(
                                        R.string.dashboard_last_checked,
                                        relativeTime(prefs.lastFetched),
                                    ),
                                )
                            }
                        }

                        else -> {
                            buildString {
                                append(
                                    stringResource(
                                        R.string.hero_stale_subtitle,
                                    ),
                                )
                                if (prefs.lastFetched > 0L) {
                                    append("\n")
                                    append(
                                        stringResource(
                                            R.string.dashboard_last_checked,
                                            relativeTime(
                                                prefs.lastFetched,
                                            ),
                                        ),
                                    )
                                }
                            }
                        }
                    },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
        IconButton(
            onClick = onRefresh,
            enabled = !prefs.isRefreshing,
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SystemEnvironmentCard(
    prefs: AppPrefsState,
    surface: Color,
    modifier: Modifier = Modifier,
) {
    val shape = Tokens.CardShape
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = surface, shape = shape)
                .clip(shape)
                .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Extension,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint =
                    if (prefs.isXposedActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.env_xposed),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text =
                        if (prefs.isXposedActive && prefs.frameworkVersion != null) {
                            stringResource(
                                R.string.env_xposed_active,
                                prefs.frameworkVersion,
                            )
                        } else if (prefs.isXposedActive) {
                            stringResource(
                                R.string.env_xposed_active,
                                "Unknown",
                            )
                        } else {
                            stringResource(R.string.env_xposed_inactive)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (prefs.isXposedActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = Build.MODEL,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${Build.MANUFACTURER} · ${Build.DEVICE}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Android,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Android ${Build.VERSION.RELEASE}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "API ${Build.VERSION.SDK_INT}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.wip_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.wip_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Construction,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.wip_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.wip_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid(
    prefs: AppPrefsState,
    surface: Color,
    onShowRules: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = Tokens.CardShape
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .background(color = surface, shape = shape)
                    .clip(shape)
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Vaccines,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.dashboard_method),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.dashboard_css_injection),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .background(color = surface, shape = shape)
                    .clip(shape)
                    .clickable(onClick = onShowRules)
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Rule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text =
                    stringResource(
                        R.string.config_active_selectors,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(
                        R.string.dashboard_rules_count,
                        prefs.selectorCount,
                    ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private val GRAYSCALE =
    ColorFilter.colorMatrix(
        ColorMatrix().apply { setToSaturation(0f) },
    )

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
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)),
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
                text = info?.second ?: stringResource(R.string.target_title),
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
    event: SelectorSyncEvent,
): String =
    when (event) {
        is SelectorSyncEvent.Updated -> {
            val total = event.added + event.removed
            context.getString(R.string.snackbar_updated, total)
        }

        is SelectorSyncEvent.UpToDate -> {
            context.getString(R.string.snackbar_up_to_date)
        }

        is SelectorSyncEvent.Error -> {
            event.message
        }
    }

@Suppress("ViewModelConstructorInComposable")
@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    PreviewWrapper {
        DashboardScreen(viewModel = PreviewAppViewModel())
    }
}

private class PreviewAppViewModel : AppViewModel() {
    private val _uiState =
        MutableStateFlow(
            AppUiState.Success(
                prefs =
                    AppPrefsState(
                        isXposedActive = true,
                        frameworkVersion = "LSPosed v1.11.0",
                        frameworkPrivilege = "Zygisk",
                        isRefreshing = false,
                        isRefreshFailed = false,
                        isStale = false,
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
    override val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    override fun refreshAll() {}

    override fun setXposedActive(
        active: Boolean,
        frameworkVersion: String?,
        frameworkPrivilege: String?,
    ) {
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
    }
}
