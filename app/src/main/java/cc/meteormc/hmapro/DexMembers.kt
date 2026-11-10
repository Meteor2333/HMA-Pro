@file:OptIn(DexKitExperimentalApi::class)

package cc.meteormc.hmapro

import android.content.pm.ApplicationInfo
import cc.meteormc.hmapro.util.DexKitCache
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.DexKitCacheBridge.CacheBridgeListener
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import org.luckypray.dexkit.query.matchers.ClassMatcher
import org.luckypray.dexkit.query.matchers.FieldMatcher
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private lateinit var dexkit: DexKitCacheBridge.RecyclableBridge

object DexMembers {
    init {
        System.loadLibrary("dexkit")
    }

    fun init(appInfo: ApplicationInfo) {
        DexKitCache.load()
        DexKitCacheBridge.init(DexKitCache)
        DexKitCacheBridge.addListener(
            object : CacheBridgeListener() {
                override fun onBridgeReleased(appTag: String) {
                    DexKitCache.trySave()
                }
            }
        )

        val path = appInfo.sourceDir
        val appHash = MessageDigest.getInstance("SHA-256").run {
            val buffer = ByteArray(8192)
            FileInputStream(path).use {
                while (true) {
                    val n = it.read(buffer)
                    if (n < 0) break
                    update(buffer, 0, n)
                }
            }
            digest().joinToString("") {
                "%02x".format(it.toInt() and 0xFF)
            }
        }
        dexkit = DexKitCacheBridge.create(appHash, path)
    }
}

object DexClasses {
    val HMAService by classFinder {
        addMethod {
            name = "<init>"
            addInvoke {
                name = "attachInterface"
            }
        }
    }

    val ServiceClient by classFinder {
        usingEqStrings("ServiceClient")
    }

    private fun classFinder(block: ClassMatcher.() -> Unit) = FinderDelegate {
        dexkit.getClass(it) { matcher(ClassMatcher().apply(block)) }.getInstance(HmaProModule.classLoader)
    }
}

object DexMethods {
    val HMAService_getServiceVersion by methodFinder {
        declaredClass = DexClasses.HMAService.typeName
        name = ServiceClient_getServiceVersion.name
        returnType = "int"
        paramCount = 0
    }

    val ServiceClient_getInstalledPackages by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingStrings("getInstalledPackages")
    }

    val ServiceClient_getLogs by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingEqStrings("getLogs")
    }

    val ServiceClient_getServiceVersion by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingEqStrings("getServiceVersion")
    }

    val ServiceClient_isNativeHookSupported by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingEqStrings("isNativeHookSupported")
    }

    val ServiceClient_clearLogs by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingStrings("clearLogs")
    }

    val ServiceClient_syncConfig by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingStrings("syncConfig")
    }

    val ServiceClient_stopService by methodFinder {
        declaredClass = DexClasses.ServiceClient.typeName
        usingStrings("stopService")
    }

    private fun constructorFinder(block: MethodMatcher.() -> Unit) = FinderDelegate {
        dexkit.getMethod(it) { matcher(MethodMatcher().apply(block)) }.getConstructorInstance(HmaProModule.classLoader)
    }

    private fun methodFinder(block: MethodMatcher.() -> Unit) = FinderDelegate {
        dexkit.getMethod(it) { matcher(MethodMatcher().apply(block)) }.getMethodInstance(HmaProModule.classLoader)
    }
}

object DexFields {
    private fun fieldFinder(block: FieldMatcher.() -> Unit) = FinderDelegate {
        dexkit.getField(it) { matcher(FieldMatcher().apply(block)) }.getFieldInstance(HmaProModule.classLoader)
    }
}

private class FinderDelegate<T : Any>(private val finder: (key: String) -> T) : ReadOnlyProperty<Any, T> {
    private var cachedValue: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (cachedValue == null) {
            val name = property.name
            val result = finder(name)
            cachedValue = result
        }
        return cachedValue!!
    }
}