package cc.meteormc.hmapro.manager.hooker

import android.content.Context
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.manager.data.HMAVersion
import cc.meteormc.hmapro.manager.util.UpdateChecker
import cc.meteormc.xposedkit.allocate
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import cc.meteormc.xposedkit.util.Primitives
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object UpdateCheckerHooker : BaseHooker<HookerContext>() {
    private val scope = MainScope()

    override fun HookerContext.hook() {
        DexMethods.UpdateInfo_fetchLatestUpdate.hookBefore {
            if (!ManagerMain.preferences.fixUpdateChecker) {
                return@hookBefore
            }

            it.result = Enum.valueOf(
                DexClasses.CoroutineSingletons,
                "COROUTINE_SUSPENDED"
            )

            val continuation = it.args[0]
            val context = DexFields.MyApp_hmaApp.get<Context>(null)
            val currentVersion = HMAVersion.current(context)
            scope.launch {
                val info = UpdateChecker.checkUpdate(currentVersion)
                val result = DexClasses.UpdateInfo.reflect {
                    val allocate = type.allocate()
                    fun initUpdateInfo(vararg values: Any): Any? {
                        val fields = declaredFields
                        if (fields.size != values.size) return null
                        values.forEachIndexed { index, value ->
                            val field = fields[index]
                            val expectedType = field.type
                            val actualType = value.javaClass
                            if (!Primitives.canAssign(actualType, expectedType)) return null
                            field.put(allocate, value)
                        }

                        return allocate
                    }

                    if (info != null) {
                        initUpdateInfo(
                            info.version,
                            currentVersion.code.toInt() + 1,
                            info.changelog,
                            info.releaseUrl
                        )
                    } else {
                        initUpdateInfo("", 0, "", "")
                    }
                } ?: return@launch
                DexMethods.Continuation_resumeWith.call(continuation, result)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Enum.Companion.valueOf(enumClass: Class<*>, name: String): Any {
        return java.lang.Enum.valueOf(enumClass as Class<out Enum<*>>, name)
    }
}