package cc.meteormc.hmapro.manager.hooker

import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext

object ServiceHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        "io.github.libxposed.service.XposedProvider".reflect {
            method("call")?.hookAfter {
                if (it.findArg<String>() != "SendBinder") {
                    return@hookAfter
                }

                ManagerMain.serviceBindEvent.tryEmit(Unit)
            }
        }
    }
}