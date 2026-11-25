package cc.meteormc.hmapro.common.hooker

import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.reflect
import org.json.JSONObject

object ConfigHooker : BaseHooker<HookerContext>() {
    override fun HookerContext.hook() {
        "icu.nullptr.hidemyapplist.common.JsonConfig".reflect {
            method("toString")?.hookAfter {
                val config = HMAConfig(it.instance!!)
                val json = JSONObject(it.result<String>())
                config.writeExtras(json)
                it.result = json.toString()
            }

            nestedClass("Companion")!!.reflect.method("parse")?.hookAfter {
                val config = HMAConfig(it.result ?: return@hookAfter)
                val json = JSONObject(it.argumentAt<String?>(0) ?: return@hookAfter)
                config.readExtras(json)
            }
        }
    }
}