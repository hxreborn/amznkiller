package eu.hxreborn.amznkiller.xposed

import android.app.ActivityManager
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.selectors.EmbeddedSelectors
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.ForceDarkHook
import eu.hxreborn.amznkiller.xposed.hook.RufusHook
import eu.hxreborn.amznkiller.xposed.hook.WebViewHook
import eu.hxreborn.amznkiller.xposed.hook.cachedSelectors
import eu.hxreborn.amznkiller.xposed.hook.installHookPrefs
import eu.hxreborn.amznkiller.xposed.hook.setFallbackSelectors
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

@PublishedApi
internal lateinit var module: AmznkillerModule
    private set

class AmznkillerModule : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        Logger.log(
            Log.INFO,
            "Module v${BuildConfig.VERSION_NAME} on $frameworkName $frameworkVersion",
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName !in AMAZON_PACKAGES || !param.isFirstPackage) return
        Logger.log(Log.INFO, "loaded pkg=${param.packageName} pid=${Process.myPid()}")

        installHookPrefs(this)

        if (cachedSelectors.isEmpty()) {
            setFallbackSelectors(EmbeddedSelectors.load())
            Logger.log(Log.INFO, "embedded fallback count=${cachedSelectors.size}")
        }

        runCatching { WebViewHook.hook(this) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=WebViewHook", it) }
        runCatching { ForceDarkHook.hook(this, param.classLoader) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=ForceDarkHook", it) }
        runCatching { RufusHook.hook(this, param.classLoader) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=RufusHook", it) }

        runCatching { showToast(param.classLoader) }
            .onFailure { Logger.log(Log.ERROR, "toast fail", it) }
    }

    private fun showToast(classLoader: ClassLoader) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                runCatching {
                    val appContext = getApplicationContext(classLoader) ?: return@postDelayed
                    val info = ActivityManager.RunningAppProcessInfo()
                    ActivityManager.getMyMemoryState(info)
                    val foregroundImportance =
                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    if (info.importance != foregroundImportance) return@postDelayed
                    Toast.makeText(appContext, TOAST_MESSAGES.random(), Toast.LENGTH_SHORT).show()
                }
            },
            TOAST_DELAY_MS,
        )
    }

    private fun getApplicationContext(classLoader: ClassLoader): Application? =
        runCatching {
            Class
                .forName("android.app.ActivityThread", false, classLoader)
                .getMethod("currentApplication")
                .invoke(null) as? Application
        }.getOrNull()

    companion object {
        const val TAG = "AmznKiller"

        val AMAZON_PACKAGES =
            setOf(
                "com.amazon.mShop.android.shopping",
                "in.amazon.mShop.android.shopping",
            )
        private const val TOAST_DELAY_MS = 1500L

        private val TOAST_MESSAGES =
            arrayOf(
                "Happy ad-free shopping",
                "No ads attached. Have fun",
                "CSS injected. You're welcome",
                "Jeff won't see you coming",
                "Ad-free mode: enabled",
            )
    }
}
