package cc.meteormc.hmapro.manager.service

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

    fun clearLogs() {
        DexMethods.IHMAService_clearLogs.call<Unit>(INSTANCE)
    }

    fun getInstalledPackages(): List<PackageInfo>? {
        val method = DexMethods.IHMAService_getInstalledPackages
        val result = method.call<Any?>(INSTANCE, 0) ?: return null
        return method.returnType.reflect {
            field(List::class.java)!!.get<List<PackageInfo>?>(result)
        }
    }

    fun getLogs(): String? {
        return DexMethods.IHMAService_getLogs.call(INSTANCE)
    }

    fun getServiceVersion(): Int {
        return DexMethods.IHMAService_getServiceVersion.call(INSTANCE)
    }

    fun isNativeHookSupported(): Boolean {
        return DexMethods.IHMAService_isNativeHookSupported.call(INSTANCE)
    }

    fun stopService(cleanEnv: Boolean) {
        DexMethods.IHMAService_stopService.call<Unit>(INSTANCE, cleanEnv)
    }

    fun syncConfig(json: String) {
        DexMethods.IHMAService_syncConfig.call<Unit>(INSTANCE, json)
    }
}