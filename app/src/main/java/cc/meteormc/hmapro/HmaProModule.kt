package cc.meteormc.hmapro

import android.util.Log
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.param.SystemServerStartingParam

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object HmaProModule : XposedModule {
    lateinit var classLoader: ClassLoader

    init {
        if (BuildConfig.DEBUG){
            XLog.updateLevel(Log.VERBOSE)
        }
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        classLoader = param.classLoader
        val packageName = param.packageName
        if (param.processName == "android") {
            SystemServerMain.init(packageName, classLoader)
        }

        if (!param.isFirstPackage) return
        if (param.packageName != Constants.HMA_PACKAGE) return
        ManagerMain.init(classLoader, param.appInfo)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        classLoader = param.classLoader
        SystemServerMain.init(Constants.SYSTEM_PACKAGE, classLoader)
    }
}