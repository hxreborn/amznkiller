package eu.hxreborn.amznkiller.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import eu.hxreborn.amznkiller.BuildConfig

object LauncherIconHelper {
    private const val ALIAS_NAME = "${BuildConfig.APPLICATION_ID}.LauncherAlias"

    fun isLauncherIconVisible(context: Context): Boolean {
        val info =
            context.packageManager.getPackageInfo(
                BuildConfig.APPLICATION_ID,
                PackageManager.GET_ACTIVITIES,
            )
        return info.activities?.any { it.targetActivity != null } == true
    }

    fun setLauncherIconVisible(
        context: Context,
        visible: Boolean,
    ) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, ALIAS_NAME),
            if (visible) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    }
}
