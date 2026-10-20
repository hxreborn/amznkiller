package eu.hxreborn.amznkiller.ui.screen.dashboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.R
import eu.hxreborn.amznkiller.prefs.ForceDarkMode
import eu.hxreborn.amznkiller.prefs.PrefSpec
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.ui.component.ForceDarkModeButtons
import eu.hxreborn.amznkiller.ui.component.PreferenceTitle
import eu.hxreborn.amznkiller.ui.component.groupedPreference
import eu.hxreborn.amznkiller.ui.component.sectionPreferenceTheme
import eu.hxreborn.amznkiller.ui.component.switchPreference
import eu.hxreborn.amznkiller.ui.preview.PreviewFixtures
import eu.hxreborn.amznkiller.ui.preview.PreviewLightDark
import eu.hxreborn.amznkiller.ui.preview.PreviewWrapper
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Loading
import eu.hxreborn.amznkiller.ui.state.DashboardUiState.Ready
import eu.hxreborn.amznkiller.ui.theme.AmznKillerSurfaceDefaults
import eu.hxreborn.amznkiller.ui.theme.Tokens
import eu.hxreborn.amznkiller.ui.util.shapeForPosition
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    updateCard: @Composable () -> Unit = {},
) {
    val uiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val state =
        when (val s = uiState) {
            Loading -> return
            is Ready -> s
        }
    val context = LocalContext.current
    var amazonPackage by remember { mutableStateOf(installedAmazonPackage(context)) }
    LifecycleResumeEffect(Unit) {
        amazonPackage = installedAmazonPackage(context)
        onPauseOrDispose {}
    }

    DashboardContent(
        state = state,
        isAmazonInstalled = amazonPackage != null,
        contentPadding = contentPadding,
        updateCard = updateCard,
        onRefresh = viewModel::refreshAll,
        onOpenAmazon = { amazonPackage?.let { openAmazon(context, it) } },
        onGetAmazon = { openPlayStore(context) },
        onSwitch = viewModel::savePref,
        onForceDarkMode = { viewModel.savePref(Prefs.FORCE_DARK_MODE, it.prefValue) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: Ready,
    isAmazonInstalled: Boolean,
    contentPadding: PaddingValues,
    updateCard: @Composable () -> Unit = {},
    onRefresh: () -> Unit,
    onOpenAmazon: () -> Unit,
    onGetAmazon: () -> Unit,
    onSwitch: (PrefSpec<Boolean>, Boolean) -> Unit,
    onForceDarkMode: (ForceDarkMode) -> Unit,
) {
    val resources = LocalResources.current
    val tagline = rememberSaveable { resources.getStringArray(R.array.taglines).random() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val status = resolveDashboardStatus(state, isAmazonInstalled)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_bar_title)) },
                subtitle = { Text(tagline, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = AmznKillerSurfaceDefaults.cardContainerColor
        ProvidePreferenceLocals(theme = sectionPreferenceTheme()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding() + Tokens.SpacingSm,
                        bottom = contentPadding.calculateBottomPadding() + Tokens.SpacingLg,
                    ),
            ) {
                item(key = "update") { updateCard() }

                item(key = "status") {
                    StatusCard(
                        status = status,
                        selectorCount = state.selectorCount,
                        lastFetched = state.lastFetched,
                        lastRefreshOutcome = state.lastRefreshOutcome,
                        onAction = {
                            when (status) {
                                DashboardStatus.AmazonMissing -> onGetAmazon()
                                DashboardStatus.Blocking -> onOpenAmazon()
                                else -> onRefresh()
                            }
                        },
                    )
                }

                preferenceCategory(
                    key = "category_ad_blocking",
                    title = { Text(stringResource(R.string.settings_ad_blocking)) },
                )

                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(1, 0)),
                    key = "remove_ads",
                    value = state.injectionEnabled,
                    icon = { Icon(Icons.Outlined.Block, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_content_filtering) },
                    summary = { Text(stringResource(R.string.settings_content_filtering_summary)) },
                    onValueChange = { onSwitch(Prefs.INJECTION_ENABLED, it) },
                )

                preferenceCategory(
                    key = "category_shopping",
                    title = { Text(stringResource(R.string.dashboard_shopping)) },
                )

                val shoppingItemCount = 4
                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(shoppingItemCount, 0)),
                    key = "price_chart_keepa",
                    value = state.priceChartKeepa,
                    icon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_price_chart_keepa) },
                    summary = { Text(stringResource(R.string.settings_price_chart_keepa_summary)) },
                    onValueChange = { onSwitch(Prefs.PRICE_CHART_KEEPA, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(shoppingItemCount, 1)),
                    key = "price_chart_camel",
                    value = state.priceChartCamel,
                    icon = { Icon(Icons.AutoMirrored.Outlined.ShowChart, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_price_chart_camel) },
                    summary = { Text(stringResource(R.string.settings_price_chart_camel_summary)) },
                    onValueChange = { onSwitch(Prefs.PRICE_CHART_CAMEL, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(shoppingItemCount, 2)),
                    key = "hide_rufus",
                    value = state.hideRufus,
                    icon = { Icon(Icons.Outlined.SmartToy, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_hide_rufus) },
                    summary = { Text(stringResource(R.string.settings_hide_rufus_summary)) },
                    onValueChange = { onSwitch(Prefs.HIDE_RUFUS, it) },
                )

                item { Spacer(Modifier.height(Tokens.PreferenceItemGap)) }

                switchPreference(
                    modifier = Modifier.groupedPreference(surface, shapeForPosition(shoppingItemCount, 3)),
                    key = "disable_video_autoplay",
                    value = state.disableVideoAutoplay,
                    icon = { Icon(Icons.Outlined.VideocamOff, contentDescription = null) },
                    title = { PreferenceTitle(R.string.settings_disable_video_autoplay) },
                    summary = { Text(stringResource(R.string.settings_disable_video_autoplay_summary)) },
                    onValueChange = { onSwitch(Prefs.DISABLE_VIDEO_AUTOPLAY, it) },
                )

                preferenceCategory(
                    key = "category_amazon_appearance",
                    title = { Text(stringResource(R.string.dashboard_amazon_appearance)) },
                )

                item(key = "force_dark_mode", contentType = "ChoicePreference") {
                    Column(modifier = Modifier.groupedPreference(surface, shapeForPosition(1, 0))) {
                        Preference(
                            icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                            title = { PreferenceTitle(R.string.settings_dark_mode) },
                            summary = { Text(stringResource(R.string.settings_dark_mode_summary)) },
                        )
                        ForceDarkModeButtons(selected = state.forceDarkMode, onSelect = onForceDarkMode)
                    }
                }
            }
        }
    }
}

private fun installedAmazonPackage(context: Context): String? =
    BuildConfig.TARGET_PACKAGES.firstOrNull { context.packageManager.getLaunchIntentForPackage(it) != null }

private fun openAmazon(
    context: Context,
    packageName: String,
) {
    context.packageManager.getLaunchIntentForPackage(packageName)?.let {
        context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun openPlayStore(context: Context) {
    val pkg = BuildConfig.TARGET_PACKAGES.first()
    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        runCatching {
            context.startActivity(intent.setData("https://play.google.com/store/apps/details?id=$pkg".toUri()))
        }
    }
}

@PreviewLightDark
@Composable
private fun DashboardContentPreview() {
    PreviewWrapper {
        DashboardContent(
            state = PreviewFixtures.dashboardReady,
            isAmazonInstalled = true,
            contentPadding = PaddingValues(0.dp),
            onRefresh = {},
            onOpenAmazon = {},
            onGetAmazon = {},
            onSwitch = { _, _ -> },
            onForceDarkMode = {},
        )
    }
}
