package cc.meteormc.hmapro.service

import android.content.pm.PackageInfo
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.reflect
import java.lang.reflect.Modifier

object ServiceClient {
    private val INSTANCE by lazy {
        DexClasses.ServiceClient.reflect {
            field {
                type = this@reflect.type
                modifiers = Modifier.STATIC
            }
        }!!.get<Any>(null)
    }

    fun getInstalledPackages(): List<PackageInfo>? {
        val method = DexMethods.ServiceClient_getInstalledPackages
        val result = method.call<Any>(INSTANCE)
        return method.returnType.reflect {
            field(List::class.java)!!.get<List<PackageInfo>?>(result)
        }
    }

    fun getLogs(): String? {
        return DexMethods.ServiceClient_getLogs.call(INSTANCE)
    }

    fun getServiceVersion(): Int {
        return DexMethods.ServiceClient_getServiceVersion.call(INSTANCE)
    }

    fun isNativeHookSupported(): Boolean {
        return DexMethods.ServiceClient_isNativeHookSupported.call(INSTANCE)
    }

    fun clearLogs() {
        DexMethods.ServiceClient_clearLogs.call<Unit>(INSTANCE)
    }

    fun syncConfig() {
        DexMethods.ServiceClient_syncConfig.call<Unit>(INSTANCE)
    }

    fun stopService() {
        DexMethods.ServiceClient_stopService.call<Unit>(INSTANCE)
    }
}