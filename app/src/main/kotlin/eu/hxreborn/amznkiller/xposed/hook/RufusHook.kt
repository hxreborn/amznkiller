package eu.hxreborn.amznkiller.xposed.hook

import android.util.Log
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface

object RufusHook {
    private const val SAVX_TAB_CONTROLLER = "com.amazon.mShop.chrome.bottomtabs.SavXTabController"

    fun hook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        runCatching {
            val controller = classLoader.loadClass(SAVX_TAB_CONTROLLER)

            xposed.hook(controller.getDeclaredMethod("isEnabled")).intercept { chain ->
                if (hideRufus) false else chain.proceed()
            }

            xposed.hook(controller.getDeclaredMethod("didTap")).intercept { chain ->
                if (hideRufus) null else chain.proceed()
            }
        }.onSuccess {
            Logger.debug { "hooked target=$SAVX_TAB_CONTROLLER" }
        }.onFailure {
            Logger.log(Log.ERROR, "hook fail target=$SAVX_TAB_CONTROLLER", it)
        }
    }
}
