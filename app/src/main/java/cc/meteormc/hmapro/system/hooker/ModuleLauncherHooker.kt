package cc.meteormc.hmapro.system.hooker

import android.os.Binder
import android.os.IInterface
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import java.util.concurrent.CountDownLatch

object ModuleLauncherHooker : BaseHooker<HookerContext>() {
    private val latch = CountDownLatch(1)

    override fun HookerContext.hook() {
        Binder::class.reflect {
            method("attachInterface")?.hookBefore {
                val owner = it.argumentAt<IInterface>(0)
                val descriptor = it.argumentAt<String>(1)
                if (descriptor != Constants.HMA_SERVICE_CLASS) {
                    return@hookBefore
                }

                XLog.d(tag, "Detected IHMAService: $owner")
                SystemServerMain.service = owner
                SystemServerMain.hmaLoader = owner.javaClass.classLoader!!
                latch.countDown()
                unhookAll()
            }
        }
    }

    fun await() = latch.await()
}