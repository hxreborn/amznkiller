package eu.hxreborn.amznkiller.xposed

import android.app.ActivityManager
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.selectors.EmbeddedSelectors
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.util.Logger
import eu.hxreborn.amznkiller.xposed.hook.ForceDarkHooker
import eu.hxreborn.amznkiller.xposed.hook.WebViewHooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.Executors

class AmznkillerModule : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        Logger.init(this)
        Logger.log("Module v${BuildConfig.VERSION_NAME} on $frameworkName $frameworkVersion")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        Logger.log("onPackageReady: ${param.packageName} isFirst=${param.isFirstPackage}")
        if (param.packageName !in AMAZON_PACKAGES || !param.isFirstPackage) return

        runCatching {
            Logger.log("Initializing PrefsManager...")
            PrefsManager.init(this)
            Logger.log(
                "PrefsManager: ${PrefsManager.selectors.size} cached selectors, " +
                    "stale=${PrefsManager.isStale()}, remotePrefs=${PrefsManager.remotePrefs != null}",
            )

            if (PrefsManager.selectors.isEmpty()) {
                Logger.log("No cached selectors, loading embedded fallback...")
                PrefsManager.setFallbackSelectors(EmbeddedSelectors.load())
                Logger.log("Embedded: ${PrefsManager.selectors.size} selectors loaded")
            }

            Logger.log("Registering WebView hooks...")
            WebViewHooker.hook(this)

            Logger.log("Registering Force Dark hooks...")
            ForceDarkHooker.hook(this, param.classLoader)

            if (PrefsManager.isStale()) {
                Logger.log("Selectors stale, submitting background refresh...")
                executor.submit {
                    runCatching {
                        val prefs =
                            PrefsManager.remotePrefs ?: run {
                                Logger.log("Background refresh: no remote prefs")
                                return@submit
                            }
                        SelectorUpdater.refresh(prefs)
                    }.onFailure {
                        Logger.log("Background refresh failed", it)
                    }
                }
            }

            showToast(param.classLoader)
        }.onFailure { Logger.log("onPackageReady failed", it) }
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
