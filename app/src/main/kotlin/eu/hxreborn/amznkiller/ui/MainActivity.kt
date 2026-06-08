package eu.hxreborn.amznkiller.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.ui.theme.AppTheme
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import eu.hxreborn.amznkiller.ui.viewmodel.AppViewModel
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Loading as SettingsLoading
import eu.hxreborn.amznkiller.ui.state.SettingsUiState.Ready as SettingsReady

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels { AppViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.settingsUiState.value is SettingsLoading
        }

        viewModel.triggerAutoUpdateIfEnabled()

        setContent {
            val settings = viewModel.settingsUiState.collectAsStateWithLifecycle()
            val (darkThemeConfig, useDynamicColor) =
                when (val s = settings.value) {
                    is SettingsLoading -> DarkThemeConfig.FOLLOW_SYSTEM to false
                    is SettingsReady -> s.darkThemeConfig to s.useDynamicColor
                }

            AppTheme(
                darkThemeConfig = darkThemeConfig,
                useDynamicColor = useDynamicColor,
            ) {
                MainScaffold(viewModel = viewModel)
            }
        }
    }

    companion object {
        @JvmStatic
        fun isXposedEnabled(): Boolean = false
    }
}
