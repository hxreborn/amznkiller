package eu.hxreborn.amznkiller.ui.screen.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.screen.dashboard.FilterUiState
import eu.hxreborn.amznkiller.ui.screen.dashboard.FilterViewModel
import eu.hxreborn.amznkiller.ui.state.FilterPrefsState
import eu.hxreborn.amznkiller.ui.state.UpdateEvent
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.theme.Tokens
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory

private val CORNER_LARGE = 24.dp
private val CORNER_SMALL = 4.dp

private fun shapeForPosition(
    count: Int,
    index: Int,
): RoundedCornerShape =
    when {
        count == 1 -> {
            RoundedCornerShape(CORNER_LARGE)
        }

        index == 0 -> {
            RoundedCornerShape(CORNER_LARGE, CORNER_LARGE, CORNER_SMALL, CORNER_SMALL)
        }

        index == count - 1 -> {
            RoundedCornerShape(CORNER_SMALL, CORNER_SMALL, CORNER_LARGE, CORNER_LARGE)
        }

        else -> {
            RoundedCornerShape(CORNER_SMALL)
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FilterViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = (uiState as? FilterUiState.Success)?.prefs ?: return
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showThemeDialog by remember { mutableStateOf(false) }

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
                    val isExpandedSlot =
                        LocalTextStyle.current.fontSize >=
                            MaterialTheme.typography.headlineMedium.fontSize
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
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
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = themeShape)
                            .clip(themeShape),
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
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.settings_advanced)) },
                )

                val advancedItemCount = 2
                val injectionShape = shapeForPosition(advancedItemCount, 0)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = injectionShape)
                            .clip(injectionShape),
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

                val debugShape = shapeForPosition(advancedItemCount, 1)
                switchPreference(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .background(color = surface, shape = debugShape)
                            .clip(debugShape),
                    key = "debug_logs",
                    value = prefs.debugLogs,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
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

private class PreviewSettingsViewModel : FilterViewModel() {
    private val _uiState =
        MutableStateFlow(
            FilterUiState.Success(
                prefs =
                    FilterPrefsState(
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
    override val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()
    private val _updateEvents = MutableSharedFlow<UpdateEvent>()
    override val updateEvents: SharedFlow<UpdateEvent> = _updateEvents

    override fun refreshAll() {}

    override fun setXposedActive(active: Boolean) {}

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
    }
}
