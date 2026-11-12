package cc.meteormc.hmapro

import cc.meteormc.hmapro.manager.ManagerEntry
import cc.meteormc.hmapro.system.SystemServerEntry
import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.param.SystemServerStartingParam

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object HmaProModule : XposedModule {
    const val HMA_PACKAGE = "com.tsng.hidemyapplist"

    lateinit var classLoader: ClassLoader

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return
        if (param.packageName != HMA_PACKAGE) return
        classLoader = param.classLoader
        ManagerEntry.init(classLoader, param.appInfo)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        classLoader = param.classLoader
        SystemServerEntry.init(classLoader)
    }
}