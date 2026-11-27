package cc.meteormc.hmapro.system.hooker

import android.app.ActivityThread
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.os.SystemClock
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object HMAHooker : BaseHooker<HookerContext>() {
    private const val TABLE_TIMEOUT = 24 * 60 * 60 * 1000L

    private var lastResolveTime = 0L
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val presetTable = HashMap<String, Set<String>>()

    override fun HookerContext.hook() {
        // todo: 换方案
        DexMethods.HMAService_compileConfig.hookAfter { info ->
            val config = HMAConfig(info.argumentAt(0))
            updateTable(config)
        }

        DexMethods.HMAService_shouldHide.hookAfter {
            if (it.result()) return@hookAfter
            val caller = it.argumentAt<String?>(0) ?: return@hookAfter
            val query = it.argumentAt<String?>(1) ?: return@hookAfter
            val shouldHide = query in (presetTable[caller] ?: return@hookAfter)
            if (shouldHide) {
                it.result = true
            }

            if (SystemClock.elapsedRealtime() - lastResolveTime >= TABLE_TIMEOUT) {
                val config = SystemServerMain.config
                if (config != null) updateTable(config)
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

                    XLog.w(tag, "Failed to register package change receiver, HMA preset table may not be updated on package changes.")
                }?.apply {
                    handle = this
                }
            }
        }
    }

    private fun updateTable(config: HMAConfig) {
        val scopes = config.scopes
        val installedPackages = getInstalledPackages()
        ioScope.launch {
            val newTable = HashMap<String, Set<String>>()
            for (app in installedPackages) {
                val packageName = app.packageName
                val scope = scopes[packageName] ?: continue
                if (scope.useWhitelist) continue

                val presets = scope.applyPresets.ifEmpty { continue }
                newTable[packageName] = presets.resolveApps(installedPackages)
            }

            withContext(Dispatchers.IO) {
                lastResolveTime = SystemClock.elapsedRealtime()
                presetTable.clear()
                presetTable.putAll(newTable)
            }
        }
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
        private var lastChange: String = ""

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
            val config = SystemServerMain.config ?: return
            val packageName = intent.data?.schemeSpecificPart
            if (packageName != null && packageName != lastChange) {
                lastChange = packageName
                updateTable(config)
            }
        }
    }
}