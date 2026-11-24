package cc.meteormc.hmapro.system

import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.system.hooker.SettingsProviderHooker
import cc.meteormc.hmapro.system.hooker.SystemContextHooker
import cc.meteormc.xposedkit.hook.HookerContext

object SystemServerMain {
    fun init(packageName: String, classLoader: ClassLoader) {
        val context = HookerContext(classLoader)
        when (packageName) {
            Constants.SYSTEM_PACKAGE -> {
                SystemContextHooker.installHook(context)
                SystemContextHooker.registerSystemContextListener {
                    val ai = it.packageManager.getApplicationInfo(Constants.HMA_PACKAGE, 0)
                    DexMembers.init(ai.sourceDir, null)
                    SettingsProviderHooker.installHook(context)
                }
            }
            Constants.SETTINGS_PROVIDER_PACKAGE -> {
                SettingsProviderHooker.installHook(context)
            }
        }
    }
}