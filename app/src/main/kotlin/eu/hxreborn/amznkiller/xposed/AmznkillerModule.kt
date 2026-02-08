package eu.hxreborn.amznkiller.xposed

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import eu.hxreborn.amznkiller.BuildConfig
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.selectors.EmbeddedSelectors
import eu.hxreborn.amznkiller.selectors.SelectorUpdater
import eu.hxreborn.amznkiller.util.Logger
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal lateinit var module: AmznkillerModule

class AmznkillerModule(
    base: XposedInterface,
    param: ModuleLoadedParam,
) : XposedModule(base, param) {
    init {
        module = this
        Logger.init(this)
        Logger.log(
            "Module v${BuildConfig.VERSION_NAME} on " +
                "${base.frameworkName} ${base.frameworkVersion}",
        )
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        Logger.log(
            "onPackageLoaded: ${param.packageName} " +
                "isFirst=${param.isFirstPackage}",
        )
        if (param.packageName != AMAZON_PACKAGE || !param.isFirstPackage) return

        runCatching {
            Logger.log("Initializing PrefsManager...")
            PrefsManager.init(this)
            Logger.log(
                "PrefsManager: ${PrefsManager.selectors.size} cached selectors, " +
                    "stale=${PrefsManager.isStale()}, " +
                    "remotePrefs=${PrefsManager.remotePrefs != null}",
            )

            if (PrefsManager.selectors.isEmpty()) {
                Logger.log("No cached selectors, loading embedded fallback...")
                PrefsManager.setFallbackSelectors(EmbeddedSelectors.load())
                Logger.log(
                    "Embedded: ${PrefsManager.selectors.size} selectors loaded",
                )
            }

            Logger.log("Registering WebView hooks...")
            WebViewHooker.hook()

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
        }.onFailure { Logger.log("onPackageLoaded failed", it) }
    }

    @Suppress("DiscouragedPrivateApi")
    private fun showToast(classLoader: ClassLoader) {
        runCatching {
            val proc =
                Application::class.java
                    .getMethod("getProcessName")
                    .invoke(null) as? String ?: return
            if (!proc.contains("amazon")) return
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    runCatching {
                        val actThread =
                            Class.forName(
                                "android.app.ActivityThread",
                                false,
                                classLoader,
                            )
                        val ctx =
                            actThread
                                .getMethod("currentApplication")
                                .invoke(null) as? Application
                                ?: return@postDelayed
                        Toast
                            .makeText(
                                ctx,
                                TOAST_MESSAGES.random(),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                },
                TOAST_DELAY_MS,
            )
        }
    }

    companion object {
        const val AMAZON_PACKAGE = "com.amazon.mShop.android.shopping"
        private const val TOAST_DELAY_MS = 1500L
        val executor: ExecutorService = Executors.newSingleThreadExecutor()

        private val TOAST_MESSAGES =
            arrayOf(
                "Happy ad-free shopping",
                "No ads attached. Have fun",
                "CSS injected. You're welcome",
                "Go forth, unsponsored",
                "Jeff won't see you coming",
                "Cleaner than he deserves",
                "Ad-free mode: engaged",
                "Godspeed, clean shopper",
            )
    }
}
