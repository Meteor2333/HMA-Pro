package cc.meteormc.hmapro.hooker

import android.app.Activity
import android.app.AppComponentFactory
import android.app.Instrumentation
import android.content.Intent
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.hmapro.ui.MainActivity
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import cc.meteormc.xposedkit.support.AppComponentFactoryWrapper

object UIHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        Instrumentation::class.reflect {
            method("getFactory")?.hookAfter {
                val pkg = it.findArg<String>()
                if (pkg != HmaProModule.HMA_PACKAGE) {
                    return@hookAfter
                }

                val factory = it.result<AppComponentFactory>()
                if (factory is ActivityInterceptor) {
                    return@hookAfter
                }

                it.result = ActivityInterceptor(factory)
            }
        }
    }

    private class ActivityInterceptor(base: AppComponentFactory) : AppComponentFactoryWrapper(base) {
        override fun instantiateActivity(cl: ClassLoader, className: String, intent: Intent?): Activity {
            if (className != "icu.nullptr.hidemyapplist.ui.activity.MainActivity") {
                return super.instantiateActivity(cl, className, intent)
            }

            return MainActivity()
        }
    }
}