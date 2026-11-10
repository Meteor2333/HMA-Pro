package cc.meteormc.hmapro.data

import android.content.Context
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.hmapro.service.ServiceClient
import cc.meteormc.xposedkit.allocate
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.reflect

sealed class ModuleStatus {
    companion object {
        private val expectedServiceVersion by lazy {
            val fake = DexClasses.HMAService.allocate()
            DexMethods.HMAService_getServiceVersion.call<Int>(fake)
        }

        fun current(context: Context): ModuleStatus {
            val isHooked = HmaProModule.classLoader.reflect("icu.nullptr.hidemyapplist.MyApp") {
                field {
                    type = Boolean::class.javaPrimitiveType
                }
            }!!.get<Boolean>(context.applicationContext)
            val serviceVersion = ServiceClient.getServiceVersion()
            return when {
                !isHooked -> Disabled
                !ServiceClient.isNativeHookSupported() -> NativeHookUnavailable
                serviceVersion == 0 -> ServiceNotActivated
                serviceVersion < expectedServiceVersion -> OutOfDate
                else -> Running(serviceVersion)
            }
        }
    }

    object Disabled : ModuleStatus()

    object OutOfDate : ModuleStatus()

    object ServiceNotActivated : ModuleStatus()

    object NativeHookUnavailable: ModuleStatus()

    class Running(val serviceVersion: Int) : ModuleStatus()
}