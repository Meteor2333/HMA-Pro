package cc.meteormc.hmapro.system.hooker

import android.os.Binder
import android.os.IInterface
import android.os.ServiceManager
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect

object ModuleLauncherHooker : BaseHooker<HookerContext>() {
    private val tasks = ArrayDeque<() -> Unit>()

    override fun HookerContext.hook() {
        ServiceManager::class.reflect {
            method("addService")?.hookBefore {
                val name = it.argumentAt<String>(0)
                if (name == "package") {
                    SystemServerMain.onPmsAttach(it.argumentAt(1))
                }
            }
        }

        Binder::class.reflect {
            method("attachInterface")?.hookBefore {
                val owner = it.argumentAt<IInterface>(0)
                val descriptor = it.argumentAt<String>(1)
                if (descriptor != Constants.HMA_SERVICE_CLASS) {
                    return@hookBefore
                }

                val unhook = SystemServerMain.onServiceAttach(owner)
                if (unhook) {
                    unhookAll()
                }
            }
        }
    }

    fun submitTask(task: () -> Unit) {
        tasks.add(task)
    }
}