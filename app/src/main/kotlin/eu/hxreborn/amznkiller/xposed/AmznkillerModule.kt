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
import eu.hxreborn.amznkiller.xposed.hook.installHookPrefs
import eu.hxreborn.amznkiller.xposed.hook.selectors
import eu.hxreborn.amznkiller.xposed.hook.setFallbackSelectors
import eu.hxreborn.amznkiller.xposed.hook.uninstallHookPrefs
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.lang.reflect.Executable

@PublishedApi
internal lateinit var module: AmznkillerModule
    private set

class AmznkillerModule : XposedModule() {
    private var targetClassLoader: ClassLoader? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        Logger.info("Module v${BuildConfig.VERSION_NAME} on $frameworkName $frameworkVersion")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName !in BuildConfig.TARGET_PACKAGES || !param.isFirstPackage) return
        Logger.info("loaded pkg=${param.packageName} pid=${Process.myPid()}")
        installHooks(param.classLoader)

        runCatching { showToast(param.classLoader) }
            .onFailure { Logger.log(Log.ERROR, "toast fail", it) }
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        uninstallHookPrefs()
        param.setSavedInstanceState(targetClassLoader)
        Logger.info("hot reloading pid=${Process.myPid()}")
        return true
    }

    override fun onHotReloaded(param: HotReloadedParam) {
        module = this
        val oldHandles = param.oldHookHandles
        oldHandles.forEach { it.unhook() }
        val classLoader = param.savedInstanceState as? ClassLoader ?: return
        Logger.info("hot reloaded v${BuildConfig.VERSION_NAME} pid=${Process.myPid()}")
        installHooks(classLoader, oldHandles.map { it.executable })
    }

    private fun installHooks(
        classLoader: ClassLoader,
        previousExecutables: List<Executable> = emptyList(),
    ) {
        targetClassLoader = classLoader
        installHookPrefs(this)

        if (selectors.isEmpty()) {
            setFallbackSelectors(EmbeddedSelectors.load())
            Logger.info("embedded fallback count=${selectors.size}")
        }

        runCatching { WebViewHook.hook(this) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=WebViewHook", it) }
        runCatching { ForceDarkHook.hook(this, classLoader, previousExecutables) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=ForceDarkHook", it) }
        runCatching { RufusHook.hook(this, classLoader) }
            .onFailure { Logger.log(Log.ERROR, "hook fail name=RufusHook", it) }
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
