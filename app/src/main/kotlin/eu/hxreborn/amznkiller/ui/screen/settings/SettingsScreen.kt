package eu.hxreborn.amznkiller.ui.screen.settings

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.SmartToy
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.component.BalloonsOverlay
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.DashboardUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Loading
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Ready
import eu.hxreborn.amznkiller.ui.theme.AmznKillerSurfaceDefaults
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
private const val SHAREHOLDER_TAPS = 7

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
    var showForceDarkModeDialog by remember { mutableStateOf(false) }
    val shareholderEgg = rememberShareholderEgg()

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

    if (showForceDarkModeDialog) {
        ForceDarkModeDialog(
            currentMode = prefs.forceDarkMode,
            onSelect = { mode ->
                viewModel.savePref(Prefs.FORCE_DARK_MODE, mode.prefValue)
                showForceDarkModeDialog = false
            },
            onDismiss = { showForceDarkModeDialog = false },
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
        val surface = AmznKillerSurfaceDefaults.cardContainerColor

        ProvidePreferenceLocals {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Tokens.ScreenHorizontalPadding),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + Tokens.SpacingLg,
                    ),
            ) {
                preferenceCategory(
                    key = "category_appearance",
                    title = { Text(stringResource(R.string.settings_appearance)) },
                )

                val appearanceItemCount = 2
                val themeShape = shapeForPosition(appearanceItemCount, 0)
                preference(
                    modifier = Modifier.preferenceModifier(surface, themeShape),
                    key = "theme",
                    icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_theme) },
                    summary = { Text(stringResource(R.string.settings_theme_summary)) },
                    onClick = { showThemeDialog = true },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val dynamicColorShape = shapeForPosition(appearanceItemCount, 1)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, dynamicColorShape),
                    key = "dynamic_color",
                    value = prefs.useDynamicColor,
                    icon = { Icon(Icons.Outlined.FormatPaint, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_dynamic_color) },
                    summary = { Text(stringResource(R.string.settings_dynamic_color_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.USE_DYNAMIC_COLOR, it) },
                )

                preferenceCategory(
                    key = "category_ad_blocking",
                    title = { Text(stringResource(R.string.settings_ad_blocking)) },
                )

                val adBlockItemCount = 3
                val filteringShape = shapeForPosition(adBlockItemCount, 0)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, filteringShape),
                    key = "css_injection",
                    value = prefs.injectionEnabled,
                    icon = { Icon(Icons.Outlined.Block, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_content_filtering) },
                    summary = { Text(stringResource(R.string.settings_content_filtering_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.INJECTION_ENABLED, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val syncShape = shapeForPosition(adBlockItemCount, 1)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, syncShape),
                    key = "auto_update",
                    value = prefs.autoUpdate,
                    icon = { Icon(Icons.Outlined.CloudSync, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_background_sync) },
                    summary = { Text(stringResource(R.string.settings_background_sync_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.AUTO_UPDATE, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val filterSourcesShape = shapeForPosition(adBlockItemCount, 2)
                preference(
                    modifier = Modifier.preferenceModifier(surface, filterSourcesShape),
                    key = "filter_sources",
                    icon = { Icon(Icons.Outlined.Link, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_filter_sources) },
                    summary = { Text(stringResource(R.string.settings_filter_sources_summary)) },
                    onClick = { showUrlDialog = true },
                )

                preferenceCategory(
                    key = "category_shopping_display",
                    title = { Text(stringResource(R.string.settings_shopping_display)) },
                )

                val displayItemCount = 3
                val chartsShape = shapeForPosition(displayItemCount, 0)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, chartsShape),
                    key = "price_charts",
                    value = prefs.priceChartsEnabled,
                    icon = { Icon(Icons.Outlined.TrendingUp, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_marketplace_insights) },
                    summary = { Text(stringResource(R.string.settings_marketplace_insights_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.PRICE_CHARTS_ENABLED, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val hideRufusShape = shapeForPosition(displayItemCount, 1)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, hideRufusShape),
                    key = "hide_rufus",
                    value = prefs.hideRufus,
                    icon = { Icon(Icons.Outlined.SmartToy, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_hide_rufus) },
                    summary = { Text(stringResource(R.string.settings_hide_rufus_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.HIDE_RUFUS, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val darkModeShape = shapeForPosition(displayItemCount, 2)
                preference(
                    modifier = Modifier.preferenceModifier(surface, darkModeShape),
                    key = "force_dark_mode",
                    icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_dark_mode) },
                    summary = { Text(forceDarkModeSummary(prefs.forceDarkMode)) },
                    onClick = { showForceDarkModeDialog = true },
                )

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.settings_advanced)) },
                )

                val advancedItemCount = 3
                val hideLauncherShape = shapeForPosition(advancedItemCount, 0)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, hideLauncherShape),
                    key = "hide_launcher_icon",
                    value = prefs.isLauncherIconHidden,
                    icon = { Icon(Icons.Outlined.PhonelinkErase, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_hide_launcher_icon) },
                    summary = { Text(stringResource(R.string.settings_hide_launcher_icon_summary)) },
                    onValueChange = { viewModel.setLauncherIconHidden(it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val webviewDebugShape = shapeForPosition(advancedItemCount, 1)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, webviewDebugShape),
                    key = "webview_debugging",
                    value = prefs.webviewDebugging,
                    icon = { Icon(Icons.Rounded.DeveloperMode, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_webview_debugging) },
                    summary = { Text(stringResource(R.string.settings_webview_debugging_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.WEBVIEW_DEBUGGING, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val debugShape = shapeForPosition(advancedItemCount, 2)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, debugShape),
                    key = "debug_logs",
                    value = prefs.debugLogs,
                    icon = { Icon(Icons.Rounded.BugReport, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_debug_logs) },
                    summary = { Text(stringResource(R.string.settings_debug_logs_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.DEBUG_LOGS, it) },
                )

                preferenceCategory(
                    key = "category_about",
                    title = { Text(stringResource(R.string.settings_about)) },
                )

                val aboutItemCount = 4
                val versionShape = shapeForPosition(aboutItemCount, 0)
                preference(
                    modifier = Modifier.preferenceModifier(surface, versionShape),
                    key = "app_version",
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_app_version) },
                    summary = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                    onClick = shareholderEgg.onVersionTap,
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val gitRepoShape = shapeForPosition(aboutItemCount, 1)
                preference(
                    modifier = Modifier.preferenceModifier(surface, gitRepoShape),
                    key = "git_repo",
                    icon = { Icon(painterResource(R.drawable.ic_github_24), contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_git_repo) },
                    summary = { Text(stringResource(R.string.settings_git_repo_summary)) },
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, REPO_URL.toUri()))
                    },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val licensesShape = shapeForPosition(aboutItemCount, 2)
                preference(
                    modifier = Modifier.preferenceModifier(surface, licensesShape),
                    key = "licenses",
                    icon = { Icon(Icons.Outlined.Gavel, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_licenses) },
                    summary = { Text(stringResource(R.string.settings_licenses_summary)) },
                    onClick = onNavigateToLicenses,
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val issueShape = shapeForPosition(aboutItemCount, 3)
                preference(
                    modifier = Modifier.preferenceModifier(surface, issueShape),
                    key = "report_issue",
                    icon = { Icon(Icons.Outlined.Feedback, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_report_issue) },
                    summary = { Text(stringResource(R.string.settings_report_issue_summary)) },
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "$REPO_URL/issues/new/choose".toUri()))
                    },
                )
            }
        }
    }

    if (shareholderEgg.showBalloons) {
        BalloonsOverlay(onDismiss = shareholderEgg.onDismissBalloons)
    }
}

@Stable
private class ShareholderEggState(
    showBalloonsProvider: () -> Boolean,
    val onDismissBalloons: () -> Unit,
    val onVersionTap: () -> Unit,
) {
    val showBalloons: Boolean by derivedStateOf(showBalloonsProvider)
}

@Composable
private fun rememberShareholderEgg(): ShareholderEggState {
    val context = LocalContext.current
    val alreadyMsg = stringResource(R.string.easter_shareholder_already)
    val countdownLabels =
        (1 until SHAREHOLDER_TAPS - 2).map { n ->
            pluralStringResource(R.plurals.easter_shareholder_countdown, n, n)
        }

    var tapCount by rememberSaveable { mutableIntStateOf(0) }
    var alreadyShareholder by rememberSaveable { mutableStateOf(false) }
    var showBalloons by remember { mutableStateOf(false) }
    val countdownToast = remember { mutableStateOf<Toast?>(null) }

    fun showToast(text: CharSequence) {
        countdownToast.value?.cancel()
        countdownToast.value = Toast.makeText(context, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    DisposableEffect(Unit) {
        onDispose { countdownToast.value?.cancel() }
    }

    return remember {
        ShareholderEggState(
            showBalloonsProvider = { showBalloons },
            onDismissBalloons = { showBalloons = false },
            onVersionTap = onVersionTap@{
                when {
                    showBalloons -> {
                        return@onVersionTap
                    }

                    alreadyShareholder -> {
                        showToast(alreadyMsg)
                    }

                    else -> {
                        tapCount += 1
                        val remaining = SHAREHOLDER_TAPS - tapCount
                        if (remaining == 0) {
                            countdownToast.value?.cancel()
                            tapCount = 0
                            alreadyShareholder = true
                            showBalloons = true
                        } else if (remaining in 1..countdownLabels.size) {
                            showToast(countdownLabels[remaining - 1])
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun PreferenceTitle(resId: Int) {
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun forceDarkModeSummary(mode: ForceDarkMode): String {
    val modeLabel =
        when (mode) {
            ForceDarkMode.OFF -> stringResource(R.string.settings_force_dark_off)
            ForceDarkMode.FOLLOW_SYSTEM -> stringResource(R.string.settings_force_dark_follow_system)
            ForceDarkMode.ON -> stringResource(R.string.settings_force_dark_on)
        }
    return stringResource(R.string.settings_dark_mode_summary_with_mode, modeLabel)
}

private fun Modifier.preferenceModifier(
    surface: Color,
    shape: Shape,
): Modifier = padding(horizontal = Tokens.ScreenHorizontalPadding).background(color = surface, shape = shape).clip(shape)

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
                forceDarkMode = ForceDarkMode.FOLLOW_SYSTEM,
            ),
        ).asStateFlow()

    override fun refreshAll() {}

    override fun triggerAutoUpdateIfEnabled() {}

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
