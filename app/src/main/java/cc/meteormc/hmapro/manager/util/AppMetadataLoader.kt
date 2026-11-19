package cc.meteormc.hmapro.manager.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object AppMetadataLoader {
    fun loadIcon(pm: PackageManager, appInfo: ApplicationInfo) {
        appInfo.loadUnbadgedIcon(pm)
    }

    fun loadLabel(pm: PackageManager, appInfo: ApplicationInfo) {
        appInfo.loadLabel(pm)
    }
}