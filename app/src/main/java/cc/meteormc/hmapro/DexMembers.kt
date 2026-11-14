@file:OptIn(DexKitExperimentalApi::class)

package cc.meteormc.hmapro

import cc.meteormc.xposedkit.util.AtomicBooleanDelegate
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.DexKitCacheBridge.CacheBridgeListener
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import org.luckypray.dexkit.query.matchers.ClassMatcher
import org.luckypray.dexkit.query.matchers.FieldMatcher
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private lateinit var dexkit: DexKitCacheBridge.RecyclableBridge

object DexMembers {
    init {
        System.loadLibrary("dexkit")
    }

    fun init(sourceDir: String, cacheFile: File?) {
        if (cacheFile != null) {
            DexKitCache.load(cacheFile)
            DexKitCacheBridge.init(DexKitCache)
            DexKitCacheBridge.addListener(
                object : CacheBridgeListener() {
                    override fun onBridgeReleased(appTag: String) {
                        DexKitCache.trySave()
                    }
                }
            )
        }

        val appHash = MessageDigest.getInstance("SHA-256").run {
            val buffer = ByteArray(8192)
            FileInputStream(sourceDir).use {
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
        dexkit = DexKitCacheBridge.create(appHash, sourceDir)
    }
}

object DexClasses {
    val ConfigManager by classFinder {
        usingEqStrings("ConfigManager")
        addFieldForType("icu.nullptr.hidemyapplist.common.JsonConfig")
    }

    val HMAService by classFinder {
        addMethod {
            name = "<init>"
            usingEqStrings("icu.nullptr.hidemyapplist.common.IHMAService")
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
    val ConfigManager_saveConfig by methodFinder {
        declaredClass = DexClasses.ConfigManager.name
        returnType = "void"
        modifiers = Modifier.STATIC
        addInvoke {
            declaredClass = ExecutorService::class.java.name
            name = "execute"
        }
    }

    val HMAService_getServiceVersion by methodFinder {
        declaredClass = DexClasses.HMAService.name
        name = ServiceClient_getServiceVersion.name
        returnType = "int"
        paramCount = 0
    }

    val ServiceClient_getInstalledPackages by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingStrings("getInstalledPackages")
    }

    val ServiceClient_getLogs by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingEqStrings("getLogs")
    }

    val ServiceClient_getServiceVersion by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingEqStrings("getServiceVersion")
    }

    val ServiceClient_isNativeHookSupported by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingEqStrings("isNativeHookSupported")
    }

    val ServiceClient_clearLogs by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingStrings("clearLogs")
    }

    val ServiceClient_syncConfig by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
        usingStrings("syncConfig")
    }

    val ServiceClient_stopService by methodFinder {
        declaredClass = DexClasses.ServiceClient.name
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
    val ConfigManager_config by fieldFinder {
        declaredClass = DexClasses.ConfigManager.name
        type = "icu.nullptr.hidemyapplist.common.JsonConfig"
        modifiers = Modifier.STATIC
    }

    val ConfigManager_INSTANCE by fieldFinder {
        declaredClass = DexClasses.ConfigManager.name
        type = DexClasses.ConfigManager.name
        modifiers = Modifier.STATIC or Modifier.FINAL
    }

    val LangList_LOCALES by fieldFinder {
        type { descriptor = "[Ljava/lang/String;" }
        modifiers = Modifier.STATIC or Modifier.FINAL
        addWriteMethod {
            name = "<clinit>"
            usingEqStrings("SYSTEM", "en")
        }
    }

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

private object DexKitCache : DexKitCacheBridge.Cache {
    private const val MAGIC = 0x444B43
    private const val VERSION = 1

    private lateinit var file: File
    private var dirty by AtomicBooleanDelegate(false)
    private val single = ConcurrentHashMap<String, String>()
    private val list = ConcurrentHashMap<String, List<String>>()

    fun load(file: File) {
        this.file = file
        if (!file.exists() || file.length() > 10 * 1024 * 1024) {
            file.writeBytes(ByteArray(0))
            return
        }

        if (file.length() <= 0) return
        DataInputStream(file.inputStream()).use { input ->
            if (input.readInt() != MAGIC) {
                throw IllegalStateException("DexKit cache file has invalid magic number, invalidating it")
            }

            if (input.readInt() != VERSION) {
                throw IllegalStateException("DexKit cache file has invalid version, invalidating it")
            }

            single.clear()
            repeat(input.readInt()) {
                val key = input.readUTF()
                val value = input.readUTF()
                single[key] = value
            }

            list.clear()
            repeat(input.readInt()) {
                val key = input.readUTF()
                val size = input.readInt()
                val value = ArrayList<String>(size)
                repeat(size) { value.add(input.readUTF()) }
                list[key] = value
            }
        }
    }

    fun trySave() {
        if (!dirty) return
        thread { save(file) }
    }

    private fun save(file: File) {
        DataOutputStream(file.outputStream()).use { output ->
            output.writeInt(MAGIC)
            output.writeInt(VERSION)

            output.writeInt(single.size)
            for ((key, value) in single) {
                output.writeUTF(key)
                output.writeUTF(value)
            }

            output.writeInt(list.size)
            for ((key, value) in list) {
                output.writeUTF(key)
                output.writeInt(value.size)
                value.forEach { output.writeUTF(it) }
            }
        }
    }

    override fun getString(key: String, default: String?): String? {
        return single[key] ?: default
    }

    override fun putString(key: String, value: String) {
        dirty = true
        single[key] = value
    }

    override fun getStringList(key: String, default: List<String>?): List<String>? {
        return list[key] ?: default
    }

    override fun putStringList(key: String, value: List<String>) {
        dirty = true
        list[key] = value
    }

    override fun remove(key: String) {
        dirty = true
        single.remove(key)
        list.remove(key)
    }

    override fun getAllKeys(): Collection<String> {
        return single.keys + list.keys
    }

    override fun clearAll() {
        dirty = true
        single.clear()
        list.clear()
    }
}