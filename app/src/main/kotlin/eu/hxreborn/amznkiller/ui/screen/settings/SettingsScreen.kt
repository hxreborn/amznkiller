package eu.hxreborn.amznkiller.ui.screen.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhonelinkErase
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Loading
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Ready
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private const val REPO_URL = "https://github.com/hxreborn/amznkiller"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onNavigateToLicenses: () -> Unit = {},
) {
    val uiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    val prefs =
        when (val s = uiState) {
            Loading -> return
            is Ready -> s
        }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }

    if (showUrlDialog) {
        SelectorUrlDialog(
            currentUrl = prefs.selectorUrl,
            onSave = { url ->
                viewModel.savePref(Prefs.SELECTOR_URL, url)
                showUrlDialog = false
            },
            onDismiss = { showUrlDialog = false },
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentConfig = prefs.darkThemeConfig,
            onSelect = { config ->
                viewModel.savePref(Prefs.DARK_THEME_CONFIG, config.name.lowercase())
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isExpandedSlot = LocalTextStyle.current.fontSize >= MaterialTheme.typography.headlineMedium.fontSize
                    Text(
                        text = stringResource(R.string.tab_settings),
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
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = MaterialTheme.colorScheme.surfaceVariant

        ProvidePreferenceLocals {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    ),
            ) {
                preferenceCategory(
                    key = "category_appearance",
                    title = { Text(stringResource(R.string.settings_appearance)) },
                )

                val appearanceItemCount = 2
                val themeShape = shapeForPosition(appearanceItemCount, 0)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = themeShape).clip(themeShape),
                    key = "theme",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_theme),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_theme_summary))
                    },
                    onClick = { showThemeDialog = true },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val dynamicColorShape = shapeForPosition(appearanceItemCount, 1)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = dynamicColorShape)
                            .clip(dynamicColorShape),
                    key = "dynamic_color",
                    value = prefs.useDynamicColor,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.FormatPaint,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_dynamic_color_summary))
                    },
                    onValueChange = { viewModel.savePref(Prefs.USE_DYNAMIC_COLOR, it) },
                )

                preferenceCategory(
                    key = "category_ad_blocking",
                    title = {
                        Text(stringResource(R.string.settings_ad_blocking))
                    },
                )

                val adBlockItemCount = 3
                val filteringShape = shapeForPosition(adBlockItemCount, 0)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = filteringShape).clip(filteringShape),
                    key = "css_injection",
                    value = prefs.injectionEnabled,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_content_filtering),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_content_filtering_summary,
                                ),
                        )
                    },
                    onValueChange = { viewModel.savePref(Prefs.INJECTION_ENABLED, it) },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val syncShape = shapeForPosition(adBlockItemCount, 1)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = syncShape).clip(syncShape),
                    key = "auto_update",
                    value = prefs.autoUpdate,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_background_sync),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_background_sync_summary,
                                ),
                        )
                    },
                    onValueChange = { viewModel.savePref(Prefs.AUTO_UPDATE, it) },
                )

                item(key = "spacer_rule_1") { Spacer(Modifier.height(2.dp)) }

                val filterSourcesShape = shapeForPosition(adBlockItemCount, 2)
                preference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = filterSourcesShape)
                            .clip(filterSourcesShape),
                    key = "filter_sources",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_filter_sources),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_filter_sources_summary,
                                ),
                        )
                    },
                    onClick = { showUrlDialog = true },
                )

                preferenceCategory(
                    key = "category_shopping_display",
                    title = {
                        Text(stringResource(R.string.settings_shopping_display))
                    },
                )

                val displayItemCount = 2
                val chartsShape = shapeForPosition(displayItemCount, 0)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = chartsShape).clip(chartsShape),
                    key = "price_charts",
                    value = prefs.priceChartsEnabled,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_marketplace_insights),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_marketplace_insights_summary,
                                ),
                        )
                    },
                    onValueChange = { viewModel.savePref(Prefs.PRICE_CHARTS_ENABLED, it) },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val darkModeShape = shapeForPosition(displayItemCount, 1)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = darkModeShape).clip(darkModeShape),
                    key = "force_dark_webview",
                    value = prefs.forceDarkWebview,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_dark_mode),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                buildAnnotatedString {
                                    append(stringResource(R.string.settings_dark_mode_summary))
                                    append("\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Note: ")
                                    }
                                    append("Requires Android 15 or higher to work properly")
                                },
                        )
                    },
                    onValueChange = { viewModel.savePref(Prefs.FORCE_DARK_WEBVIEW, it) },
                )

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.settings_advanced)) },
                )

                val advancedItemCount = 3
                val hideLauncherShape = shapeForPosition(advancedItemCount, 0)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = hideLauncherShape)
                            .clip(hideLauncherShape),
                    key = "hide_launcher_icon",
                    value = prefs.isLauncherIconHidden,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PhonelinkErase,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_hide_launcher_icon),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_hide_launcher_icon_summary))
                    },
                    onValueChange = { viewModel.setLauncherIconHidden(it) },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val webviewDebugShape = shapeForPosition(advancedItemCount, 1)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = webviewDebugShape)
                            .clip(webviewDebugShape),
                    key = "webview_debugging",
                    value = prefs.webviewDebugging,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.DeveloperMode,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_webview_debugging),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text =
                                stringResource(
                                    R.string.settings_webview_debugging_summary,
                                ),
                        )
                    },
                    onValueChange = {
                        viewModel.savePref(Prefs.WEBVIEW_DEBUGGING, it)
                    },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val debugShape = shapeForPosition(advancedItemCount, 2)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = debugShape).clip(debugShape),
                    key = "debug_logs",
                    value = prefs.debugLogs,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_debug_logs),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_debug_logs_summary))
                    },
                    onValueChange = { viewModel.savePref(Prefs.DEBUG_LOGS, it) },
                )

                preferenceCategory(
                    key = "category_about",
                    title = { Text(stringResource(R.string.settings_about)) },
                )

                val aboutItemCount = 4
                val versionShape = shapeForPosition(aboutItemCount, 0)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = versionShape).clip(versionShape),
                    key = "app_version",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_app_version),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        )
                    },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val gitRepoShape = shapeForPosition(aboutItemCount, 1)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = gitRepoShape).clip(gitRepoShape),
                    key = "git_repo",
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_github_24),
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_git_repo),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_git_repo_summary))
                    },
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, REPO_URL.toUri()),
                        )
                    },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val licensesShape = shapeForPosition(aboutItemCount, 2)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = licensesShape).clip(licensesShape),
                    key = "licenses",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Gavel,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_licenses),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_licenses_summary))
                    },
                    onClick = onNavigateToLicenses,
                )

                item { Spacer(Modifier.height(2.dp)) }

                val issueShape = shapeForPosition(aboutItemCount, 3)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = issueShape).clip(issueShape),
                    key = "report_issue",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Feedback,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_report_issue),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_report_issue_summary))
                    },
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "$REPO_URL/issues/new/choose".toUri()),
                        )
                    },
                )
            }
        }
    }
}

internal inline fun LazyListScope.switchPreference(
    key: String,
    value: Boolean,
    crossinline title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline enabled: (Boolean) -> Boolean = { true },
    noinline icon: @Composable ((Boolean) -> Unit)? = null,
    noinline summary: @Composable ((Boolean) -> Unit)? = null,
    noinline onValueChange: (Boolean) -> Unit,
) {
    item(key = key, contentType = "SwitchPreference") {
        SwitchPreference(
            value = value,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled(value),
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            onValueChange = onValueChange,
        )
    }
}

@Suppress("ViewModelConstructorInComposable")
@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    PreviewWrapper {
        SettingsScreen(viewModel = PreviewSettingsViewModel())
    }
}

private class PreviewSettingsViewModel : AppViewModel() {
    override val dashboardUiState: StateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState.Loading).asStateFlow()

    override val settingsUiState: StateFlow<SettingsUiState> =
        MutableStateFlow(
            Ready(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = true,
                debugLogs = false,
                injectionEnabled = true,
                forceDarkWebview = false,
            ),
        ).asStateFlow()

    override fun refreshAll() {}

    override fun setXposedActive(
        active: Boolean,
        frameworkVersion: String?,
    ) {
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
    }

    override fun setLauncherIconHidden(hidden: Boolean) {}
}
