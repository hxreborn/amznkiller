package eu.hxreborn.amznkiller.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.amznkiller.App
import eu.hxreborn.amznkiller.prefs.Prefs
import eu.hxreborn.amznkiller.prefs.PrefsRepositoryImpl
import eu.hxreborn.amznkiller.ui.screen.dashboard.AppViewModel
import eu.hxreborn.amznkiller.ui.screen.dashboard.AppViewModelFactory
import eu.hxreborn.amznkiller.ui.screen.dashboard.FilterUiState
import eu.hxreborn.amznkiller.ui.theme.AppTheme
import eu.hxreborn.amznkiller.ui.theme.DarkThemeConfig
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class MainActivity :
    ComponentActivity(),
    XposedServiceHelper.OnServiceListener {
    private var remotePrefs: SharedPreferences? = null

    private val viewModel: AppViewModel by viewModels {
        AppViewModelFactory(
            PrefsRepositoryImpl(
                localPrefs = getSharedPreferences(Prefs.GROUP, MODE_PRIVATE),
                remotePrefsProvider = { remotePrefs },
            ),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        App.addServiceListener(this)

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val prefs = (uiState.value as? FilterUiState.Success)?.prefs

            AppTheme(
                darkThemeConfig = prefs?.darkThemeConfig ?: DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = prefs?.useDynamicColor ?: true,
            ) {
                MainScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onServiceBind(service: XposedService) {
        remotePrefs = service.getRemotePreferences(Prefs.GROUP)
        val privilege = service.frameworkPrivilege
        viewModel.setXposedActive(
            active = true,
            frameworkVersion = "${service.frameworkName} v${service.frameworkVersion}",
            frameworkPrivilege =
                when (privilege) {
                    XposedService.Privilege.FRAMEWORK_PRIVILEGE_ROOT -> {
                        "Zygisk"
                    }

                    else -> {
                        privilege.name
                            .removePrefix("FRAMEWORK_PRIVILEGE_")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                    }
                },
        )
    }

    override fun onServiceDied(service: XposedService) {
        remotePrefs = null
        viewModel.setXposedActive(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.removeServiceListener(this)
    }

    companion object {
        @JvmStatic
        fun isXposedEnabled(): Boolean = false
    }
}
