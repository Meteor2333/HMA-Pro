package cc.meteormc.hmapro

import android.util.Log
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.param.HotReloadingParam
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.param.ProcessLoadedParam
import cc.meteormc.xposedkit.param.SystemServerStartingParam

@ModuleRegister(
    targetApi = 102,
    staticScope = true,
    autoHotReload = true
)
object HmaProModule : XposedModule {
    init {
        if (BuildConfig.DEBUG){
            XLog.updateLevel(Log.VERBOSE)
        }
    }

    private var isSystemServer = false
    private var hotReloadData: Map<String, Any>? = null

    override fun onProcessLoaded(param: ProcessLoadedParam) {
        super.onProcessLoaded(param)
        isSystemServer = param.isSystemServer
        if (param.isHotReload) {
            hotReloadData = param.hotReloadInfo!!.savedData()
        }
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        val packageName = param.packageName
        val classLoader = param.classLoader
        if (isSystemServer) {
            SystemServerMain.init(
                packageName,
                classLoader,
                hotReloadData
            )
        }

        if (!param.isFirstPackage) return
        if (packageName != Constants.HMA_PACKAGE) return
        ManagerMain.init(classLoader, param.appInfo)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        SystemServerMain.init(
            Constants.SYSTEM_PACKAGE,
            param.classLoader,
            hotReloadData
        )
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        if (isSystemServer) {
            param.savedData = SystemServerMain.reload()
            return true
        }

        return false
    }
}