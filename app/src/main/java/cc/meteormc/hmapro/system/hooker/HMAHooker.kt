package cc.meteormc.hmapro.system.hooker

import android.content.pm.PackageInfo
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
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import kotlinx.coroutines.runBlocking

object HMAHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        // todo: 计划将钩子改为在compileConfig时触发 起一个异步任务来解析预设
        //       然后在shouldHide方法里来修改逻辑 这样就尽可能不动已有的HMA预编译配置设计
        //       同时需要加缓存（可带超时） 并且在有应用安装或卸载也重新在后台解析一次
        DexMethods.HMAService_compileConfig.hookAfter { info ->
            val config = HMAConfig(info.argumentAt(0))
            val result = info.result<Map<String, Any>>()

            val scopes = config.scopes
            for ((packageName, value) in result) {
                val scope = scopes[packageName] ?: continue
                if (scope.useWhitelist) continue
                if (!DexClasses.CompiledAppConfig.isInstance(value)) continue

                val presets = scope.applyPresets.ifEmpty { continue }
                val querySet = DexFields.CompiledAppConfig_querySet.get<MutableSet<String>>(value)
                val source = getInstalledPackages()
                XLog.d(tag, source.joinToString("\n") { it.packageName })
                val resolved = runBlocking {
                    presets.resolveApps(source)
                }
                XLog.d(tag, resolved.joinToString())
                querySet.addAll(resolved)
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
}