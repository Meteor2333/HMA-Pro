package cc.meteormc.hmapro.common.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import cc.meteormc.hmapro.common.HMAConfig.AppPreset
import cc.meteormc.xposedkit.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipFile

object PresetResolver {
    suspend fun AppPreset.resolveApps(source: List<PackageInfo>): List<PackageInfo> {
        val filter = when (this) {
            AppPreset.DETECTORS -> DetectorFilter
            AppPreset.SHIZUKU_APPS -> ShizukuAppFilter
            AppPreset.XPOSED_MODULES -> XposedModuleFilter
        }
        return withContext(Dispatchers.IO) {
            source.filter { filter.shouldFilter(it) }
        }
    }

    suspend fun Collection<AppPreset>.resolveApps(source: List<PackageInfo>): List<String> {
        val filters = map {
            when (it) {
                AppPreset.DETECTORS -> DetectorFilter
                AppPreset.SHIZUKU_APPS -> ShizukuAppFilter
                AppPreset.XPOSED_MODULES -> XposedModuleFilter
            }
        }
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<String>()
            for (app in source) {
                val appInfo = app.applicationInfo
                if (appInfo != null && appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    continue
                }

                XLog.d("PresetResolver", app.packageName)
                for (filter in filters) {
                    if (!filter.shouldFilter(app)) continue
                    XLog.d("PresetResolver", "shouldHide")
                    result.add(app.packageName)
                    break
                }
            }

            result
        }
    }

    private fun checkApk(appInfo: ApplicationInfo, vararg entries: String): Boolean {
        fun openApk(path: String): Boolean {
            ZipFile(path).use {
                for (entry in entries) {
                    if (it.getEntry(entry) == null) {
                        continue
                    }

                    return true
                }
            }

            return false
        }

        if (openApk(appInfo.sourceDir)) {
            return true
        }

        val split = appInfo.splitSourceDirs
        if (!split.isNullOrEmpty()) {
            for (path in split) {
                if (!openApk(path)) continue
                return true
            }
        }

        return false
    }

    private interface PresetFilter {
        fun shouldFilter(info: PackageInfo): Boolean
    }

    private object DetectorFilter : PresetFilter {
        private val detectorPackages = setOf(
            "com.eltavine.duckdetector",
            "com.zhenxi.hunter",
            "io.github.vvb2060.mahoshojo",
            "icu.nullptr.nativetest",
            "icu.nullptr.applistdetector",
            "com.byxiaorun.detector",
            "gr.nikolasspyr.integritycheck"
        )

        override fun shouldFilter(info: PackageInfo): Boolean {
            return info.packageName in detectorPackages
        }
    }

    private object ShizukuAppFilter : PresetFilter {
        override fun shouldFilter(info: PackageInfo): Boolean {
            return false
        }
    }

    private object XposedModuleFilter : PresetFilter {
        override fun shouldFilter(info: PackageInfo): Boolean {
            val appInfo = info.applicationInfo ?: return false
            return checkApk(
                appInfo,
                "assets/xposed_init",
                "META-INF/xposed/module.prop"
            )
        }
    }
}