package eu.hxreborn.amznkiller

import android.app.Application
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        log("onCreate: registering listener")
        XposedServiceHelper.registerListener(
            object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    log("onServiceBind: $service")
                    App.service = service
                    listeners.forEach { it.onServiceBind(service) }
                }

                override fun onServiceDied(service: XposedService) {
                    log("onServiceDied")
                    App.service = null
                    listeners.forEach { it.onServiceDied(service) }
                }
            },
        )
    }

    companion object {
        private const val TAG = "AmznKiller"

        lateinit var instance: App
            private set

        var service: XposedService? = null
            private set

        private val listeners = CopyOnWriteArrayList<XposedServiceHelper.OnServiceListener>()

        private fun log(msg: String) {
            if (BuildConfig.DEBUG) Log.d(TAG, msg)
        }

        fun addServiceListener(listener: XposedServiceHelper.OnServiceListener) {
            listeners.add(listener)
            service?.let { listener.onServiceBind(it) }
        }

        fun removeServiceListener(listener: XposedServiceHelper.OnServiceListener) {
            listeners.remove(listener)
        }
    }
}
