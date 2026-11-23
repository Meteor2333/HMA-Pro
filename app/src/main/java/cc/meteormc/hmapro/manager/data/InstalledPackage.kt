package cc.meteormc.hmapro.manager.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.preference.AppSortMethod
import cc.meteormc.hmapro.manager.util.AppMetaLoader
import cc.meteormc.xposedkit.util.AtomicDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledPackage(
    val label: String,
    val icon: Bitmap,
    val packageName: String,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    companion object {
        private var cache by AtomicDelegate(emptyList<InstalledPackage>())

        suspend fun getInstalledPackages(
            packageManager: PackageManager,
            scope: CoroutineScope = MainScope(),
            useCache: Boolean = true
        ): List<InstalledPackage> {
            if (useCache && cache.isNotEmpty()) {
                return cache
            }

            val packages = withContext(Dispatchers.IO) {
                ServiceClient.getInstalledPackages().orEmpty()
            }.mapNotNull {
                val appInfo = it.applicationInfo ?: return@mapNotNull null
                InstalledPackage(
                    label = AppMetaLoader.loadLabel(packageManager, appInfo, it.lastUpdateTime),
                    icon = AppMetaLoader.loadUnbadgedIcon(packageManager, appInfo, it.lastUpdateTime),
                    packageName = it.packageName,
                    isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                    firstInstallTime = it.firstInstallTime,
                    lastUpdateTime = it.lastUpdateTime
                )
            }.sortedWith(
                AppSortMethod.BY_LABEL.comparator
            )

            scope.launch {
                AppMetaLoader.saveCache()
            }

            cache = packages
            return packages
        }
    }
}