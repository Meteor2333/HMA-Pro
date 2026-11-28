package cc.meteormc.hmapro.system.hooker

import android.app.ActivityThread
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.os.Handler
import android.os.Looper
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.common.util.PresetResolver.resolveApps
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookHandle
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object HMAHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        DexMethods.HMAService_compileConfig.hookAfter { info ->
            val config = HMAConfig(info.argumentAt(0))
            val result = info.result<Map<String, Any>>()
            val resolved = runBlocking { resolvePresets(config) }

            for ((packageName, apps) in resolved) {
                val compiledConfig = result[packageName] ?: continue
                if (!DexClasses.CompiledAppConfig.isInstance(compiledConfig)) continue
                val querySet = DexFields.CompiledAppConfig_querySet.get<MutableSet<String>>(compiledConfig)
                querySet.addAll(apps)
            }
        }

        if (!PackageChangeReceiver.tryRegister()) {
            var handle: HookHandle? = null
            "com.android.server.SystemServer".reflect {
                method("createSystemContext")?.hookAfter {
                    handle?.unhook()

                    if (PackageChangeReceiver.tryRegister()) {
                        return@hookAfter
                    }

                    XLog.w(tag, "Failed to register package change receiver, preset apps may not be updated on package changes.")
                }?.apply {
                    handle = this
                }
            }
        }
    }

    private suspend fun resolvePresets(config: HMAConfig): Map<String, Set<String>> {
        val scopes = config.scopes
        val startTime = System.currentTimeMillis()
        val installedPackages = getInstalledPackages()
        val result = HashMap<String, Set<String>>()
        for (app in installedPackages) {
            val packageName = app.packageName
            val scope = scopes[packageName] ?: continue
            if (scope.useWhitelist) continue

            val presets = scope.applyPresets.ifEmpty { continue }
            result[packageName] = presets.resolveApps(installedPackages)
        }

        XLog.d(tag, "Resolved presets in ${System.currentTimeMillis() - startTime}ms")
        return result
    }

    private fun getInstalledPackages(): List<PackageInfo> {
        val service = SystemServerMain.service
        val method = DexMethods.HMAService_getInstalledPackages
        val result = method.call<Any>(service, 0)
        return method.returnType.reflect {
            field(List::class.java)!!.get<List<PackageInfo>?>(result).orEmpty()
        }
    }

    private object PackageChangeReceiver : BroadcastReceiver() {
        private var future: ScheduledFuture<*>? = null
        private val handler = Handler(Looper.getMainLooper())
        private val scheduler = Executors.newSingleThreadScheduledExecutor()

        init {
            SystemServerMain.registerCleaner {
                handler.removeCallbacksAndMessages(null)
                scheduler.shutdownNow()
            }
        }

        fun tryRegister(): Boolean {
            val at = ActivityThread.currentActivityThread() ?: return false
            val context = at.systemContext as? Context? ?: return false
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            context.registerReceiver(this, filter)
            return true
        }

        override fun onReceive(context: Context, intent: Intent) {
            future?.cancel(false)

            if (scheduler.isShutdown) return
            future = scheduler.schedule(
                { updateConfig() },
                5000L,
                TimeUnit.MILLISECONDS
            )
        }

        private fun updateConfig() = handler.post {
            val service = SystemServerMain.service
            val config = SystemServerMain.config?.config ?: return@post
            DexMethods.HMAService_syncConfig.call(service, config.toString())
        }
    }
}