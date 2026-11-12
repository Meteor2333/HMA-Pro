package cc.meteormc.hmapro.system.hooker

import android.content.Context
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.util.WeakDelegate

object SystemContextHooker : BaseHooker<HookerContext>() {
    private var currentContext by WeakDelegate<Context>()
    private val listeners = mutableListOf<(Context) -> Unit>()

    override fun HookerContext.hook() {
        "android.app.ActivityThread".reflect {
            val currentActivityThread = method("currentActivityThread") ?: return
            val getSystemContext = method("getSystemContext") ?: return

            "com.android.server.SystemServer".reflect {
                method("createSystemContext")?.hookAfter {
                    val at = currentActivityThread.call<Any>(null)
                    val context = getSystemContext.call<Context>(at)
                    currentContext = context
                    listeners.forEach { it(context) }
                }
            }
        }
    }

    fun registerSystemContextListener(listener: (Context) -> Unit) {
        val cur = currentContext
        if (cur != null) {
            listener(cur)
            return
        }

        listeners.add(listener)
    }
}