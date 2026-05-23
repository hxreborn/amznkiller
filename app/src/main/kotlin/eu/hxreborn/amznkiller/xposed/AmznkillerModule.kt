package eu.hxreborn.amznkiller.xposed

import android.app.ActivityManager
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.selectors.EmbeddedSelectors
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.ForceDarkHooker
import eu.hxreborn.amznkiller.xposed.hook.RufusHooker
import eu.hxreborn.amznkiller.xposed.hook.WebViewHooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.Executors

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
        Logger.log(Log.INFO, "loaded for ${param.packageName} pid=${Process.myPid()}")

        runCatching {
            PrefsManager.init(this)
            Logger.debug {
                "PrefsManager: ${PrefsManager.selectors.size} cached selectors, " +
                    "stale=${PrefsManager.isStale()}, remotePrefs=${PrefsManager.remotePrefs != null}"
            }

            if (PrefsManager.selectors.isEmpty()) {
                Logger.debug { "No cached selectors, loading embedded fallback" }
                PrefsManager.setFallbackSelectors(EmbeddedSelectors.load())
                Logger.debug { "Embedded: ${PrefsManager.selectors.size} selectors loaded" }
            }

            WebViewHooker.hook(this)
            ForceDarkHooker.hook(this, param.classLoader)
            RufusHooker.hook(this, param.classLoader)

            if (PrefsManager.isStale()) {
                Logger.debug { "Selectors stale, submitting background refresh" }
                executor.submit {
                    runCatching {
                        val prefs =
                            PrefsManager.remotePrefs ?: run {
                                Logger.debug { "Background refresh: no remote prefs" }
                                return@submit
                            }
                        SelectorUpdater.refresh(prefs)
                    }.onFailure {
                        Logger.log(Log.ERROR, "Background refresh failed", it)
                    }
                }
            }

            showToast(param.classLoader)
        }.onFailure { Logger.log(Log.ERROR, "onPackageReady failed", it) }
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
        val AMAZON_PACKAGES =
            setOf(
                "com.amazon.mShop.android.shopping",
                "in.amazon.mShop.android.shopping",
            )
        private const val TOAST_DELAY_MS = 1500L
        private val executor = Executors.newSingleThreadExecutor()

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
