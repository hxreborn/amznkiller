package eu.hxreborn.amznkiller.ui.screen.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.selectors.MergeResult
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.dashboard.AppUiState
import eu.hxreborn.amznkiller.ui.screen.dashboard.AppViewModel
import eu.hxreborn.amznkiller.ui.state.AppPrefsState
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = (uiState as? AppUiState.Success)?.prefs ?: return
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
                        maxLines = if (isExpandedSlot) Tokens.ExpandedTitleMaxLines else 1,
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
                    key = "category_rule_management",
                    title = { Text(stringResource(R.string.settings_rule_management)) },
                )

                val ruleItemCount = 2
                val filterSourcesShape = shapeForPosition(ruleItemCount, 0)
                preference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = filterSourcesShape)
                            .clip(filterSourcesShape),
                    key = "filter_sources",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
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

                item(key = "spacer_rule_1") { Spacer(Modifier.height(2.dp)) }

                val syncShape = shapeForPosition(ruleItemCount, 1)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = syncShape).clip(syncShape),
                    key = "auto_update",
                    value = prefs.autoUpdate,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
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

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.settings_advanced)) },
                )

                val advancedItemCount = 3
                val injectionShape = shapeForPosition(advancedItemCount, 0)
                switchPreference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = injectionShape).clip(injectionShape),
                    key = "css_injection",
                    value = prefs.injectionEnabled,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.control_css_injection),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = {
                        Text(text = stringResource(R.string.settings_css_injection_summary))
                    },
                    onValueChange = { viewModel.savePref(Prefs.INJECTION_ENABLED, it) },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val webviewDebugShape = shapeForPosition(advancedItemCount, 1)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                color = surface,
                                shape = webviewDebugShape,
                            ).clip(webviewDebugShape),
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
                val gitRepoShape = shapeForPosition(aboutItemCount, 0)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = gitRepoShape).clip(gitRepoShape),
                    key = "git_repo",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.DataObject,
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

                val licensesShape = shapeForPosition(aboutItemCount, 1)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = licensesShape).clip(licensesShape),
                    key = "licenses",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Description,
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

                val versionShape = shapeForPosition(aboutItemCount, 2)
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
                    onClick = {
                        val clip =
                            ClipData.newPlainText(
                                "version",
                                "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            )
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(clip)
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.settings_version_copied),
                                Toast.LENGTH_SHORT,
                            ).show()
                    },
                )

                item { Spacer(Modifier.height(2.dp)) }

                val issueShape = shapeForPosition(aboutItemCount, 3)
                preference(
                    modifier = Modifier.padding(horizontal = 8.dp).background(color = surface, shape = issueShape).clip(issueShape),
                    key = "report_issue",
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_report_issue),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "$REPO_URL/issues".toUri()),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ThemeDialog(
    currentConfig: DarkThemeConfig,
    onSelect: (DarkThemeConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = DarkThemeConfig.entries
    val labels =
        listOf(
            stringResource(R.string.settings_theme_system),
            stringResource(R.string.settings_theme_light),
            stringResource(R.string.settings_theme_dark),
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = option == currentConfig,
                                    onClick = { onSelect(option) },
                                    role = Role.RadioButton,
                                ).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == currentConfig,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.padding(start = 16.dp))
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun SelectorUrlDialog(
    currentUrl: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by remember { mutableStateOf(currentUrl) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_selector_url_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        testResult = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (isTesting) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(
                            text = stringResource(R.string.settings_selector_url_testing),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                testResult?.let { result ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (result.startsWith("Failed")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        if (url.isNotBlank() && !isTesting) {
                            isTesting = true
                            testResult = null
                            scope.launch {
                                val result =
                                    withContext(Dispatchers.IO) {
                                        runCatching { SelectorUpdater.fetchMerged(url) }
                                    }
                                isTesting = false
                                testResult =
                                    result.fold(
                                        onSuccess = { mergeResult ->
                                            when (mergeResult) {
                                                is MergeResult.Success -> {
                                                    "${mergeResult.selectors.size} selectors found"
                                                }

                                                is MergeResult.Partial -> {
                                                    "Partial: ${mergeResult.selectors.size} selectors (remote failed)"
                                                }
                                            }
                                        },
                                        onFailure = { "Failed: ${it.message}" },
                                    )
                            }
                        }
                    },
                    enabled = url.isNotBlank() && !isTesting,
                ) {
                    Text(stringResource(R.string.settings_selector_url_test))
                }
                TextButton(
                    onClick = {
                        url = Prefs.SELECTOR_URL.default
                        testResult = null
                    },
                ) {
                    Text(stringResource(R.string.settings_selector_url_reset))
                }
                TextButton(
                    onClick = { onSave(url) },
                    enabled = url.isNotBlank(),
                ) {
                    Text(stringResource(R.string.settings_selector_url_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_licenses)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        LibrariesContainer(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
}

private inline fun LazyListScope.switchPreference(
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
    private val _uiState =
        MutableStateFlow(
            AppUiState.Success(
                prefs =
                    AppPrefsState(
                        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                        useDynamicColor = true,
                        debugLogs = false,
                        isXposedActive = true,
                        selectorCount = 42,
                        lastFetched = System.currentTimeMillis() - 3_600_000,
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
