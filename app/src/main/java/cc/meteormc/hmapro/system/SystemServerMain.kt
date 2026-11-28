package cc.meteormc.hmapro.system

import android.content.pm.IPackageManager
import android.os.Build
import android.os.IInterface
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.common.hooker.ConfigHooker
import cc.meteormc.hmapro.system.hooker.HMAHooker
import cc.meteormc.hmapro.system.hooker.ModuleLauncherHooker
import cc.meteormc.hmapro.system.hooker.SettingsProviderHooker
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

object SystemServerMain {
    private const val TAG = "SystemServerMain"

    lateinit var service: Any
    lateinit var hmaLoader: ClassLoader
    lateinit var pms: IPackageManager
    private var isInitialized = AtomicBoolean(false)
    private val launchTasks = ArrayDeque<() -> Unit>()
    private val cleanerRegistry = ArrayDeque<() -> Unit>()
    var config: HMAConfig? = null
        get() {
            val config = DexFields.HMAService_config.get<Any?>(service)
            if (config == null) {
                field = null
                return null
            }

            if (field?.config !== config) {
                field = HMAConfig(config)
            }

            return field
        }
        private set

    fun init(
        packageName: String,
        classLoader: ClassLoader,
        reloadData: Map<String, Any>?
    ) {
        if (packageName == Constants.SYSTEM_PACKAGE) {
            val context = HookerContext(classLoader)
            ModuleLauncherHooker.installHook(context)
        }
        
        val hookers = when (packageName) {
            Constants.SYSTEM_PACKAGE -> listOf(
                ConfigHooker,
                HMAHooker
            )
            Constants.SETTINGS_PROVIDER_PACKAGE -> listOf(
                ConfigHooker,
                SettingsProviderHooker
            )
            else -> return
        }

        if (isInitialized.get()) {
            launch(classLoader, hookers)
            return
        }

        if (reloadData != null) {
            onPmsAttach(reloadData["pms"] as IPackageManager)
            onServiceAttach(reloadData["service"] as IInterface)
            launch(classLoader, hookers)

            val rawConfig = reloadData["config"]
            if (rawConfig != null) {
                config?.readExtras(rawConfig as JSONObject)
            }
            return
        }

        launchTasks.add {
            launch(classLoader, hookers)
        }
    }

    fun reload(): Map<String, Any>? {
        if (!isInitialized.get()) {
            return null
        }

        while (cleanerRegistry.isNotEmpty()) {
            cleanerRegistry.removeFirst().invoke()
        }

        return buildMap {
            this["service"] = service
            this["pms"] = pms
            if (config != null) {
                this["config"] = config!!.rawJson
            }
        }
    }

    fun registerCleaner(block: () -> Unit) {
        cleanerRegistry.add(block)
    }
    
    internal fun onPmsAttach(pms: IPackageManager) {
        this.pms = pms
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pms.getPackageInfo(Constants.HMA_PACKAGE, 0L, 0)
        } else {
            pms.getPackageInfo(Constants.HMA_PACKAGE, 0, 0)
        }

        val appInfo = packageInfo.applicationInfo
        if (appInfo == null) {
            XLog.e(TAG, "Failed to get ApplicationInfo for HMA")
            return
        }

        val bootClassLoader = Any::class.java.classLoader!!
        DexMembers.init(
            appInfo.sourceDir,
            bootClassLoader,
            null
        )
    }
    
    internal fun onServiceAttach(service: IInterface): Boolean {
        XLog.d(TAG, "Detected IHMAService: $service")

        val hmaLoader = service.javaClass.classLoader!!
        DexMembers.classLoader = hmaLoader
        try {
            if (!DexClasses.HMAService.isInstance(service)) {
                XLog.w(TAG, "Service $service is not an instance of HMAService")
                return false
            }
        } catch (_: Throwable) {
            return false
        }

        if (isInitialized.compareAndSet(false, true)) {
            SystemServerMain.service = service
            SystemServerMain.hmaLoader = hmaLoader
            while (launchTasks.isNotEmpty()) {
                launchTasks.removeFirst().invoke()
            }
            return true
        }

        return false
    }

    private fun launch(
        classLoader: ClassLoader,
        hookers: List<BaseHooker<HookerContext>>
    ) {
        val context = HookerContext(CompositeClassLoader(classLoader))
        hookers.forEach { it.installHook(context) }
    }

    private class CompositeClassLoader(base: ClassLoader) : ClassLoader(base) {
        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            if (name.startsWith("icu.nullptr.hidemyapplist")) {
                Class.forName(name, resolve, hmaLoader)
            }

            return try {
                super.loadClass(name, resolve)
            } catch (e: ClassNotFoundException) {
                Class.forName(name, resolve, hmaLoader)
            }
        }
    }
}