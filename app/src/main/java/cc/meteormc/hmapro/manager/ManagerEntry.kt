package cc.meteormc.hmapro.manager

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.hmapro.manager.hooker.ActivityHooker
import cc.meteormc.hmapro.manager.hooker.ServiceHooker
import cc.meteormc.hmapro.manager.hooker.UiHooker
import cc.meteormc.xposedkit.hook.HookerContext
import java.io.File

object ManagerEntry {
    fun init(classLoader: ClassLoader, appInfo: ApplicationInfo) {
        @SuppressLint("SdCardPath")
        DexMembers.init(
            appInfo.sourceDir,
            File("/data/data/${HmaProModule.HMA_PACKAGE}/files/hmapro_cache.dat")
        )
        val context = HookerContext(classLoader)
        ActivityHooker.installHook(context)
        ServiceHooker.installHook(context)
        UiHooker.installHook(context)
    }
}