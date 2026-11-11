package cc.meteormc.hmapro.hooker

import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import kotlinx.coroutines.flow.MutableSharedFlow

object ServiceHooker : BaseHooker<HookerContext>() {
    val eventFlow = MutableSharedFlow<Unit>(replay = 1)

    override fun HookerContext.hook() {
        "io.github.libxposed.service.XposedProvider".reflect {
            method("call")?.hookAfter {
                if (it.findArg<String>() != "SendBinder") {
                    return@hookAfter
                }

                eventFlow.tryEmit(Unit)
            }
        }
    }
}