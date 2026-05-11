package eu.hxreborn.amznkiller.xposed.hook

import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

object RufusHooker {
    private const val SAVX_TAB_CONTROLLER = "com.amazon.mShop.chrome.bottomtabs.SavXTabController"

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        runCatching {
            val cls = classLoader.loadClass(SAVX_TAB_CONTROLLER)

            // Static isEnabled() — controls whether the Rufus tab appears in the bottom nav
            val isEnabledMethod = cls.getDeclaredMethod("isEnabled")
            xposed.hook(isEnabledMethod).intercept { chain ->
                if (PrefsManager.hideRufus) false else chain.proceed()
            }

            // didTap() — called on tab select/reselect/unselect, launches the Rufus panel
            val didTapMethod = cls.getDeclaredMethod("didTap")
            xposed.hook(didTapMethod).intercept { chain ->
                if (PrefsManager.hideRufus) null else chain.proceed()
            }

            Logger.log("RufusHooker: hooked $SAVX_TAB_CONTROLLER")
        }.onFailure { Logger.log("RufusHooker: failed to hook", it) }
    }
}
