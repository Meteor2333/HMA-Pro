@file:OptIn(DexKitExperimentalApi::class)

package cc.meteormc.hmapro

import android.app.Activity
import android.content.Context
import android.os.IInterface
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.util.AtomicBooleanDelegate
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.DexKitCacheBridge.CacheBridgeListener
import org.luckypray.dexkit.annotations.DexKitExperimentalApi
import org.luckypray.dexkit.query.enums.UsingType
import org.luckypray.dexkit.query.matchers.ClassMatcher
import org.luckypray.dexkit.query.matchers.FieldMatcher
import org.luckypray.dexkit.query.matchers.MethodMatcher
import org.luckypray.dexkit.result.ClassDataList
import org.luckypray.dexkit.result.FieldDataList
import org.luckypray.dexkit.result.MethodDataList
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object DexMembers {
    const val TAG = "DexMembers"

    lateinit var classLoader: ClassLoader
    lateinit var dexkit: DexKitCacheBridge.RecyclableBridge

    init {
        System.loadLibrary("dexkit")

        DexKitCacheBridge.init(DexKitCache)
        DexKitCacheBridge.addListener(
            object : CacheBridgeListener() {
                override fun onBridgeReleased(appTag: String) {
                    DexKitCache.trySave()
                }
            }
        )
    }

    fun init(
        sourceDir: String,
        classLoader: ClassLoader,
        cacheFile: File?
    ) {
        val startTime = System.currentTimeMillis()
        if (cacheFile != null) {
            DexKitCache.load(cacheFile)
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

        this.classLoader = classLoader
        this.dexkit = DexKitCacheBridge.create(appHash, sourceDir)
        XLog.d(TAG, "DexKit initialized in ${System.currentTimeMillis() - startTime}ms")
    }
}

object DexClasses {
    val CompiledAppConfig by classFinder {
        addFieldForType(LinkedHashSet::class.java)
        addMethod {
            name = "<init>"
            addCaller(
                MethodMatcher.create(DexMethods.HMAService_compileConfig)
            )
        }
    }

    val ConfigManager by classFinder {
        usingEqStrings("ConfigManager")
        addFieldForType("icu.nullptr.hidemyapplist.common.JsonConfig")
    }

    val CoroutineSingletons by classFinder {
        superClass = Enum::class.java.name
        addMethod {
            usingEqStrings(
                "COROUTINE_SUSPENDED",
                "UNDECIDED",
                "RESUMED"
            )
        }
    }

    val HMAService by classFinder {
        addMethod {
            name = "<init>"
            usingEqStrings(Constants.HMA_SERVICE_CLASS)
            addInvoke {
                name = "attachInterface"
            }
        }
    }

    val IHMAService by classDirectFinder {
        val clientClass = getClassData(ServiceClient)!!
        findClass {
            searchIn(clientClass.interfaces)
            matcher {
                addInterface(IInterface::class.java.name)
            }
        }
    }

    val OnPreferenceChangeListener by classFinder {
        modifiers = Modifier.INTERFACE
        methodCount(1)
        addMethod {
            addParamType("androidx.preference.Preference")
            addParamType(Any::class.java)
            returnType = "boolean"
        }
    }

    val ServiceClient by classFinder {
        usingEqStrings("ServiceClient")
    }

    val UpdateInfo by classFinder {
        addFieldForType(String::class.java)
        addFieldForType(Int::class.javaPrimitiveType!!)
        addFieldForType(String::class.java)
        addFieldForType(String::class.java)
        addMethod {
            name = "<init>"
            addCaller(
                MethodMatcher.create(DexMethods.UpdateInfo_fetchLatestUpdate)
            )
        }
    }

    private fun classFinder(block: ClassMatcher.() -> Unit) = FinderDelegate {
        val clazz = DexMembers.dexkit.getClass(it) {
            matcher(ClassMatcher().apply(block))
        }
        return@FinderDelegate clazz.getInstance(DexMembers.classLoader)
    }

    private fun classDirectFinder(block: DexKitBridge.() -> ClassDataList) = FinderDelegate {
        val clazz = DexMembers.dexkit.getClassDirect(it) {
            block(this).single()
        }
        return@FinderDelegate clazz.getInstance(DexMembers.classLoader)
    }
}

object DexMethods {
    val ConfigManager_saveConfig by methodFinder {
        declaredClass(DexClasses.ConfigManager)
        returnType = "void"
        modifiers = Modifier.STATIC
        addInvoke {
            declaredClass(Executor::class.java)
            name = "execute"
        }
    }

    val Continuation_resumeWith by methodFinder {
        val matcher = MethodMatcher().apply {
            paramTypes(Any::class.java)
            returnType = "void"
        }
        declaredClass {
            modifiers = Modifier.INTERFACE
            methodCount(2)
            addMethod {
                paramCount = 0
                returnType {
                    modifiers = Modifier.INTERFACE
                }
            }
            addMethod(matcher)
        }

        allOf(matcher)
    }

    val Fragment_requireActivity by methodFinder {
        returnType {
            superClass {
                superClass = Activity::class.java.name
            }
        }
        usingEqStrings(" not attached to an activity.")
    }

    val Fragment_requireContext by methodFinder {
        returnType(Context::class.java)
        usingEqStrings(" not attached to a context.")
    }

    val HMAService_compileConfig by methodFinder {
        declaredClass(DexClasses.HMAService)
        modifiers = Modifier.STATIC
        paramTypes("icu.nullptr.hidemyapplist.common.JsonConfig")
        returnType(Map::class.java)
    }

    val IHMAService_clearLogs by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingStrings("clearLogs")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_getInstalledPackages by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingStrings("getInstalledPackages")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_getLogs by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingEqStrings("getLogs")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_getServiceVersion by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingEqStrings("getServiceVersion")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_isNativeHookSupported by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingEqStrings("isNativeHookSupported")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_stopService by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingStrings("stopService")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val IHMAService_syncConfig by methodDirectFinder {
        val childMethod = findMethod {
            matcher {
                declaredClass(DexClasses.ServiceClient)
                usingStrings("syncConfig")
            }
        }.single()
        findMethod {
            matcher {
                descriptor = childMethod.descriptor
                declaredClass(DexClasses.IHMAService)
            }
        }
    }

    val JsonConfig_parse by methodFinder {
        declaredClass = $$"icu.nullptr.hidemyapplist.common.JsonConfig$Companion"
        name = "parse"
    }

    val PreferenceFragmentCompat_findPreference by methodFinder {
        declaredClass {
            superClass {
                addMethod(
                    MethodMatcher.create(Fragment_requireActivity)
                )
                addMethod(
                    MethodMatcher.create(Fragment_requireContext)
                )
            }
        }

        paramTypes(String::class.java)
        returnType = "androidx.preference.Preference"
    }

    val SettingsPreferenceFragment_onCreatePreferences by methodFinder {
        declaredClass = $$"icu.nullptr.hidemyapplist.ui.fragment.SettingsFragment$SettingsPreferenceFragment"
        addInvoke {
            usingEqStrings("This should be called after super.onCreate.")
        }
    }

    val TwoStatePreference_setChecked by methodFinder {
        declaredClass = "androidx.preference.TwoStatePreference"
        paramTypes(Boolean::class.javaPrimitiveType!!)
        returnType = "void"
        addUsingField {
            usingType = UsingType.Read
            type = "boolean"
        }
        addUsingField {
            usingType = UsingType.Write
            type = "boolean"
        }
    }

    val UpdateInfo_fetchLatestUpdate by methodFinder {
        usingStrings("https://api.nullptr.icu/")
        paramCount = 1
        returnType(Any::class.java)
    }

    private fun constructorFinder(block: MethodMatcher.() -> Unit) = FinderDelegate {
        val ctor = DexMembers.dexkit.getMethod(it) {
            matcher(MethodMatcher().apply(block))
        }
        return@FinderDelegate ctor.getConstructorInstance(DexMembers.classLoader)
    }

    private fun constructorDirectFinder(block: DexKitBridge.() -> MethodDataList) = FinderDelegate {
        val ctor = DexMembers.dexkit.getMethodDirect(it) {
            block(this).single()
        }
        return@FinderDelegate ctor.getConstructorInstance(DexMembers.classLoader)
    }

    private fun methodFinder(block: MethodMatcher.() -> Unit) = FinderDelegate {
        val method = DexMembers.dexkit.getMethod(it) {
            matcher(MethodMatcher().apply(block))
        }
        return@FinderDelegate method.getMethodInstance(DexMembers.classLoader)
    }

    private fun methodDirectFinder(block: DexKitBridge.() -> MethodDataList) = FinderDelegate {
        val method = DexMembers.dexkit.getMethodDirect(it) {
            block(this).single()
        }
        return@FinderDelegate method.getMethodInstance(DexMembers.classLoader)
    }
}

object DexFields {
    val CompiledAppConfig_querySet by fieldFinder {
        declaredClass(DexClasses.CompiledAppConfig)
        type(LinkedHashSet::class.java)
    }

    val ConfigManager_config by fieldFinder {
        declaredClass(DexClasses.ConfigManager)
        type = "icu.nullptr.hidemyapplist.common.JsonConfig"
        modifiers = Modifier.STATIC
    }

    val ConfigManager_INSTANCE by fieldFinder {
        declaredClass(DexClasses.ConfigManager)
        type = DexClasses.ConfigManager.name
        modifiers = Modifier.STATIC or Modifier.FINAL
    }

    val HMAService_config by fieldFinder {
        declaredClass(DexClasses.HMAService)
        type = "icu.nullptr.hidemyapplist.common.JsonConfig"
    }

    val JsonConfig_Companion by fieldFinder {
        declaredClass = "icu.nullptr.hidemyapplist.common.JsonConfig"
        type = $$"icu.nullptr.hidemyapplist.common.JsonConfig$Companion"
        modifiers = Modifier.STATIC or Modifier.FINAL
    }

    val LangList_LOCALES by fieldFinder {
        type(Array<String>::class.java)
        modifiers = Modifier.STATIC or Modifier.FINAL
        addWriteMethod {
            name = "<clinit>"
            usingEqStrings("SYSTEM", "en")
        }
    }

    val MyApp_hmaApp by fieldFinder {
        type = "icu.nullptr.hidemyapplist.MyApp"
        modifiers = Modifier.STATIC
    }

    private fun fieldFinder(block: FieldMatcher.() -> Unit) = FinderDelegate {
        val field = DexMembers.dexkit.getField(it) {
            matcher(FieldMatcher().apply(block))
        }
        return@FinderDelegate field.getFieldInstance(DexMembers.classLoader)
    }

    private fun fieldDirectFinder(block: DexKitBridge.() -> FieldDataList) = FinderDelegate {
        val field = DexMembers.dexkit.getFieldDirect(it) {
            block(this).single()
        }
        return@FinderDelegate field.getFieldInstance(DexMembers.classLoader)
    }
}

private class FinderDelegate<T : Any>(private val finder: (key: String) -> T) : ReadOnlyProperty<Any, T> {
    private var cachedValue: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (cachedValue == null) {
            val startTime = System.currentTimeMillis()
            val name = property.name
            val result = finder(name)
            cachedValue = result
            XLog.d(DexMembers.TAG, "Found $name in ${System.currentTimeMillis() - startTime}ms: $result")
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
            file.parentFile?.mkdirs()
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
        if (!::file.isInitialized) return
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