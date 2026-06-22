package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.ui.preview.FakeAppViewModel
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Loading
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Ready
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val state =
        when (val s = uiState) {
            Loading -> return
            is Ready -> s
        }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val resources = LocalResources.current
    val tagline =
        rememberSaveable {
            resources.getStringArray(R.array.taglines).random()
        }

    val (amazonPackage, amazonInfo) =
        remember {
            val packageManager = context.packageManager
            BuildConfig.TARGET_PACKAGES.firstNotNullOfOrNull { pkg ->
                runCatching {
                    val info = packageManager.getPackageInfo(pkg, 0)
                    val icon = packageManager.getApplicationIcon(pkg).toBitmap(128, 128).asImageBitmap()
                    val label =
                        info.applicationInfo
                            ?.let(packageManager::getApplicationLabel)
                            ?.toString()
                            .orEmpty()
                    pkg to TargetAppData(icon, label, info.versionName)
                }.getOrNull()
            } ?: (null to null)
        }

    val webViewInfo =
        remember {
            runCatching {
                val webViewPackage = WebView.getCurrentWebViewPackage() ?: return@runCatching null
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(webViewPackage.packageName, 0)
                val icon = packageManager.getApplicationIcon(appInfo).toBitmap(128, 128).asImageBitmap()
                val label = packageManager.getApplicationLabel(appInfo).toString()
                TargetAppData(icon, label, webViewPackage.versionName)
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
                            maxLines = if (isExpandedSlot) Tokens.EXPANDED_TITLE_MAX_LINES else 1,
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
    ) { innerPadding ->
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
                    isRefreshing = state.isRefreshing,
                    isRefreshFailed = state.isRefreshFailed,
                    isStale = state.isStale,
                    lastFetched = state.lastFetched,
                    lastRefreshOutcome = state.lastRefreshOutcome,
                    onRefresh = { viewModel.refreshAll() },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                TargetAppsCard(
                    amazonPackage = amazonPackage,
                    amazonInfo = amazonInfo,
                    webViewInfo = webViewInfo,
                    surface = surface,
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                MetricsGrid(
                    injectionEnabled = state.injectionEnabled,
                    selectorCount = state.selectorCount,
                    surface = surface,
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SystemEnvironmentCard(
                    isXposedActive = state.isXposedActive,
                    frameworkVersion = state.frameworkVersion,
                    surface = surface,
                )
            }
        }
    }
}

@Suppress("ViewModelConstructorInComposable")
@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    val app = LocalContext.current.applicationContext as android.app.Application
    PreviewWrapper {
        DashboardScreen(viewModel = FakeAppViewModel(app))
    }
}
