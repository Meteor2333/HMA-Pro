package cc.meteormc.hmapro.system.hooker

import cc.meteormc.hmapro.DexClasses
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect

object TestHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        DexClasses.HMAService.reflect {

        }
    }
}