package cc.meteormc.hmapro.xposed

import cc.meteormc.xposedkit.XposedModule
import cc.meteormc.xposedkit.annotation.ModuleRegister
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.param.PackageLoadedParam
import cc.meteormc.hmapro.xposed.hooker.UIHooker

@ModuleRegister(
    targetApi = 102,
    staticScope = true
)
object HmaProModule : XposedModule {
    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (!param.isFirstPackage) return
        val context = HookerContext(param.classLoader)
        UIHooker.installHook(context)
    }
}