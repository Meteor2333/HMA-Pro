package cc.meteormc.hmapro.system

import android.content.pm.IPackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMembers
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.system.hooker.ModuleLauncherHooker
import cc.meteormc.hmapro.system.hooker.SettingsProviderHooker
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object SystemServerMain {
    private const val TAG = "SystemServerMain"

    private var isInitialized = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    lateinit var service: Any
    lateinit var hmaLoader: ClassLoader
    var config: HMAConfig? = null
        get() {
            val config = DexFields.HMAService_config.get<Any?>(service)
            if (config == null) {
                field = null
                return null
            }

            if (field?.config != config) {
                field = HMAConfig(config)
            }

            return field
        }
        private set

    init {
        val bootClassLoader = Any::class.java.classLoader!!
        val context = HookerContext(bootClassLoader)
        ModuleLauncherHooker.installHook(context)
    }

    fun init(packageName: String, classLoader: ClassLoader) {
        val hookers = when (packageName) {
            Constants.SYSTEM_PACKAGE -> listOf(

            )
            Constants.SETTINGS_PROVIDER_PACKAGE -> listOf(
                SettingsProviderHooker
            )
            else -> return
        }

        thread {
            ModuleLauncherHooker.await()
            mainHandler.post { launch(classLoader, hookers) }
        }
    }

    fun launch(
        classLoader: ClassLoader,
        hookers: List<BaseHooker<HookerContext>>
    ) {
        if (isInitialized.compareAndSet(false, true)) {
            val packageInfo = service.javaClass.reflect {
                val pms = field(IPackageManager::class.java)!!.get<IPackageManager>(service)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pms.getPackageInfo(Constants.HMA_PACKAGE, 0L, 0)
                } else {
                    pms.getPackageInfo(Constants.HMA_PACKAGE, 0, 0)
                }
            }

            val appInfo = packageInfo.applicationInfo
            if (appInfo == null) {
                XLog.e(TAG, "Failed to get ApplicationInfo for HMA")
                return
            }

            DexMembers.init(appInfo.sourceDir, hmaLoader, null)
        }

        val context = HookerContext(classLoader)
        hookers.forEach { it.installHook(context) }
    }
}