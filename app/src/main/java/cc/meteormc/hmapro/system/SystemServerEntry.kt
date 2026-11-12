package cc.meteormc.hmapro.system

import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.hmapro.system.hooker.SystemContextHooker
import cc.meteormc.hmapro.system.hooker.TestHooker
import cc.meteormc.xposedkit.hook.HookerContext

object SystemServerEntry {
    fun init(classLoader: ClassLoader) {
        val context = HookerContext(classLoader)
        SystemContextHooker.installHook(context)
        SystemContextHooker.registerSystemContextListener {
            val ai = it.packageManager.getApplicationInfo(HmaProModule.HMA_PACKAGE, 0)
            DexMembers.init(ai.sourceDir, null)
            launch(context)
        }
    }

    private fun launch(context: HookerContext) {
        TestHooker.installHook(context)
    }
}