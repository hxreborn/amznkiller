package eu.hxreborn.amznkiller.xposed.hook

import android.util.Log
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

object RufusHooker {
    private const val TAG = "RufusHooker"
    private const val SAVX_TAB_CONTROLLER = "com.amazon.mShop.chrome.bottomtabs.SavXTabController"

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        runCatching {
            val controller = classLoader.loadClass(SAVX_TAB_CONTROLLER)

            xposed.hook(controller.getDeclaredMethod("isEnabled")).intercept { chain ->
                if (PrefsManager.hideRufus) false else chain.proceed()
            }

            xposed.hook(controller.getDeclaredMethod("didTap")).intercept { chain ->
                if (PrefsManager.hideRufus) null else chain.proceed()
            }
        }.onSuccess {
            Logger.debug { "$TAG: hooked $SAVX_TAB_CONTROLLER" }
        }.onFailure {
            Logger.log(Log.ERROR, "$TAG: failed to hook $SAVX_TAB_CONTROLLER", it)
        }
    }
}
