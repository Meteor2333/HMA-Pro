package cc.meteormc.hmapro.manager

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.pm.ApplicationInfo
import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.manager.hooker.ActivityHooker
import cc.meteormc.hmapro.manager.hooker.BackupHooker
import cc.meteormc.hmapro.manager.hooker.ServiceHooker
import cc.meteormc.hmapro.manager.hooker.UiHooker
import cc.meteormc.hmapro.manager.hooker.UpdateCheckerHooker
import cc.meteormc.hmapro.manager.ui.preference.PreferencesState
import cc.meteormc.xposedkit.XposedKit
import cc.meteormc.xposedkit.hook.HookerContext
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File

object ManagerMain {
    @SuppressLint("SdCardPath")
    const val CACHE_DIR = "/data/data/${Constants.HMA_PACKAGE}/cache/hmapro"
    const val MAIN_ACTIVITY = "icu.nullptr.hidemyapplist.ui.activity.MainActivity"

    lateinit var classLoader: ClassLoader
        private set
    lateinit var preferences: PreferencesState
        private set
    val serviceBindEvent = MutableSharedFlow<Unit>(replay = 1)

    fun init(classLoader: ClassLoader, appInfo: ApplicationInfo) {
        this.classLoader = classLoader
        @SuppressLint("SdCardPath")
        DexMembers.init(
            appInfo.sourceDir,
            classLoader,
            File(CACHE_DIR, "dexkit_cache.dat")
        )
        val context = HookerContext(classLoader)
        ActivityHooker.installHook(context)
        BackupHooker.installHook(context)
        ServiceHooker.installHook(context)
        UiHooker.installHook(context)
        UpdateCheckerHooker.installHook(context)
        XposedKit.registerAppAttachListener(appInfo.packageName) {
            preferences = PreferencesState(
                it.getSharedPreferences("settings", MODE_PRIVATE)
            )
        }
    }
}