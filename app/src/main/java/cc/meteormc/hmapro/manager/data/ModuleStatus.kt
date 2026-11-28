package cc.meteormc.hmapro.manager.data

import android.content.Context
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.xposedkit.allocate
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.reflect

sealed class ModuleStatus {
    companion object {
        private val expectedServiceVersion by lazy {
            val fake = DexClasses.HMAService.allocate()
            DexMethods.IHMAService_getServiceVersion.call<Int>(fake)
        }

        fun current(context: Context): ModuleStatus {
            val isHooked = ManagerMain.classLoader.reflect("icu.nullptr.hidemyapplist.MyApp") {
                field {
                    type = Boolean::class.javaPrimitiveType
                }
            }!!.get<Boolean>(context.applicationContext)
            val serviceVersion = ServiceClient.getServiceVersion()
            return when {
                !isHooked -> Disabled
                serviceVersion == 0 -> ServiceNotRunning
                serviceVersion < expectedServiceVersion -> OutOfDate
                else -> Running(serviceVersion)
            }
        }
    }

    object Disabled : ModuleStatus()

    object OutOfDate : ModuleStatus()

    object ServiceNotRunning : ModuleStatus()

    class Running(val serviceVersion: Int) : ModuleStatus()
}