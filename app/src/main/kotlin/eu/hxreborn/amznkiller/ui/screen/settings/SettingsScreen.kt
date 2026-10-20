package eu.hxreborn.amznkiller.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhonelinkErase
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.preview.FakeAppViewModel
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.update.UpdateRowAction
import eu.hxreborn.amznkiller.ui.screen.update.UpdateRowSummary
import eu.hxreborn.amznkiller.ui.screen.update.UpdateSettingsActions
import eu.hxreborn.amznkiller.ui.screen.update.settingsRowAction
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Loading
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Ready
import eu.hxreborn.amznkiller.ui.theme.AmznKillerSurfaceDefaults
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import eu.hxreborn.amznkiller.ui.viewmodel.UpdateUiState
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.preferenceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onNavigateToAbout: () -> Unit = {},
    updateState: UpdateUiState? = null,
    updateActions: UpdateSettingsActions? = null,
) {
    val uiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    val prefs =
        when (val s = uiState) {
            Loading -> return
            is Ready -> s
        }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showUrlDialog by rememberSaveable { mutableStateOf(false) }

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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.tab_settings)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = AmznKillerSurfaceDefaults.cardContainerColor

        ProvidePreferenceLocals(
            theme =
                preferenceTheme(
                    categoryColor = MaterialTheme.colorScheme.primary,
                    categoryTextStyle = MaterialTheme.typography.labelLargeEmphasized,
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Tokens.ScreenHorizontalPadding),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + Tokens.SpacingLg,
                    ),
            ) {
                if (updateState != null && updateActions != null) {
                    preferenceCategory(
                        key = "category_updates",
                        title = { Text(stringResource(R.string.settings_updates)) },
                    )

                    preference(
                        modifier = Modifier.preferenceModifier(surface, shapeForPosition(2, 0)),
                        key = "update_check",
                        icon = { Icon(Icons.Outlined.SystemUpdate, contentDescription = null) },
                        title = { PreferenceTitle(R.string.update_check) },
                        summary = { UpdateRowSummary(updateState) },
                        widgetContainer = { UpdateRowAction(updateState, updateActions) },
                        onClick = updateState.settingsRowAction(updateActions),
                    )

                    item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                    switchPreference(
                        modifier = Modifier.preferenceModifier(surface, shapeForPosition(2, 1)),
                        key = "update_auto_check",
                        value = updateState.autoCheckUpdates,
                        icon = { Icon(Icons.Outlined.Autorenew, contentDescription = null) },
                        title = { PreferenceTitle(R.string.update_auto_check) },
                        onValueChange = updateActions.onAutoCheck,
                    )
                }

                preferenceCategory(
                    key = "category_appearance",
                    title = { Text(stringResource(R.string.settings_appearance)) },
                )

                val appearanceItemCount = 2
                val themeShape = shapeForPosition(appearanceItemCount, 0)
                item(key = "theme", contentType = "ChoicePreference") {
                    Column(modifier = Modifier.preferenceModifier(surface, themeShape)) {
                        Preference(
                            icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                            title = { PreferenceTitle(R.string.settings_theme) },
                        )
                        ChoiceButtonGroup(
                            options = DarkThemeConfig.entries,
                            selected = prefs.darkThemeConfig,
                            onSelect = { viewModel.savePref(Prefs.DARK_THEME_CONFIG, it.name.lowercase()) },
                            label = { themeLabel(it) },
                            modifier = choiceButtonGroupModifier,
                        )
                    }
                }

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

                val displayItemCount = 4
                val chartsShape = shapeForPosition(displayItemCount, 0)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, chartsShape),
                    key = "price_charts",
                    value = prefs.priceChartsEnabled,
                    icon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_marketplace_insights) },
                    summary = { Text(stringResource(R.string.settings_marketplace_insights_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.PRICE_CHARTS_ENABLED, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val autoplayShape = shapeForPosition(displayItemCount, 1)
                switchPreference(
                    modifier = Modifier.preferenceModifier(surface, autoplayShape),
                    key = "disable_video_autoplay",
                    value = prefs.disableVideoAutoplay,
                    icon = { Icon(Icons.Outlined.VideocamOff, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_disable_video_autoplay) },
                    summary = { Text(stringResource(R.string.settings_disable_video_autoplay_summary)) },
                    onValueChange = { viewModel.savePref(Prefs.DISABLE_VIDEO_AUTOPLAY, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                val hideRufusShape = shapeForPosition(displayItemCount, 2)
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

                val darkModeShape = shapeForPosition(displayItemCount, 3)
                item(key = "force_dark_mode", contentType = "ChoicePreference") {
                    Column(modifier = Modifier.preferenceModifier(surface, darkModeShape)) {
                        Preference(
                            icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                            title = { PreferenceTitle(R.string.settings_dark_mode) },
                            summary = { Text(stringResource(R.string.settings_dark_mode_summary)) },
                        )
                        ChoiceButtonGroup(
                            options = ForceDarkMode.entries,
                            selected = prefs.forceDarkMode,
                            onSelect = { viewModel.savePref(Prefs.FORCE_DARK_MODE, it.prefValue) },
                            label = { forceDarkModeLabel(it) },
                            modifier = choiceButtonGroupModifier,
                        )
                    }
                }

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

                preference(
                    modifier = Modifier.preferenceModifier(surface, shapeForPosition(1, 0)),
                    key = "about",
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    title = { PreferenceTitle(R.string.app_name) },
                    summary = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                    onClick = onNavigateToAbout,
                )
            }
        }
    }
}

@Composable
private fun PreferenceTitle(resId: Int) {
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.bodyLarge,
    )
}

private val choiceButtonGroupModifier =
    Modifier.padding(start = Tokens.SpacingLg, end = Tokens.SpacingLg, bottom = Tokens.SpacingLg)

@Composable
private fun themeLabel(config: DarkThemeConfig): String =
    stringResource(
        when (config) {
            DarkThemeConfig.FOLLOW_SYSTEM -> R.string.settings_theme_system
            DarkThemeConfig.LIGHT -> R.string.settings_theme_light
            DarkThemeConfig.DARK -> R.string.settings_theme_dark
        },
    )

@Composable
private fun forceDarkModeLabel(mode: ForceDarkMode): String =
    stringResource(
        when (mode) {
            ForceDarkMode.OFF -> R.string.settings_force_dark_off
            ForceDarkMode.FOLLOW_SYSTEM -> R.string.settings_force_dark_follow_system
            ForceDarkMode.ON -> R.string.settings_force_dark_on
        },
    )

private fun Modifier.preferenceModifier(
    surface: Color,
    shape: Shape,
): Modifier = padding(horizontal = Tokens.ScreenHorizontalPadding).background(color = surface, shape = shape)

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
        val isEnabled = enabled(value)
        Preference(
            title = { title(value) },
            modifier = modifier.toggleable(value, isEnabled, Role.Switch, onValueChange = onValueChange),
            enabled = isEnabled,
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            widgetContainer = {
                val theme = LocalPreferenceTheme.current
                Switch(
                    checked = value,
                    onCheckedChange = null,
                    modifier =
                        Modifier.padding(
                            start = theme.horizontalSpacing,
                            top = theme.padding.calculateTopPadding(),
                            end = theme.padding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = theme.padding.calculateBottomPadding(),
                        ),
                    enabled = isEnabled,
                    thumbContent = {
                        Icon(
                            imageVector = if (value) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                )
            },
        )
    }
}

@Suppress("ViewModelConstructorInComposable")
@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    val app = LocalContext.current.applicationContext as android.app.Application
    PreviewWrapper {
        SettingsScreen(viewModel = FakeAppViewModel(app))
    }
}
