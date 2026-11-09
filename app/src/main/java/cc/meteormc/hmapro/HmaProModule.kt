package cc.meteormc.hmapro

import cc.meteormc.hmapro.hooker.UIHooker
import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.xposedkit.param.SystemServerStartingParam

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object HmaProModule : XposedModule {
    const val HMA_PACKAGE = "com.tsng.hidemyapplist"

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return
        if (param.packageName != HMA_PACKAGE) return
        val context = HookerContext(param.classLoader)
        UIHooker.installHook(context)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {

    }
}