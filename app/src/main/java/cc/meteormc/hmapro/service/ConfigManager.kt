package cc.meteormc.hmapro.service

import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect

object ConfigManager {
    private lateinit var config_: JsonConfig
    private val config: JsonConfig
        get() {
            if (!::config_.isInitialized) {
                val cm = DexFields.ConfigManager_INSTANCE.get<Any>(null)
                val delegate = DexFields.ConfigManager_config.get<Any?>(cm)
                    ?: throw IllegalStateException("ConfigManager.config is null")
                config_ = JsonConfig(delegate)
            }

            return config_
        }

    private fun saveConfig() {
        DexMethods.ConfigManager_saveConfig.call<Unit>(config)
    }

    var detailLog: Boolean
        get() = config["detailLog"]
        set(value) {
            config["detailLog"] = value
            saveConfig()
        }

    var maxLogSize: Int
        get() = config["maxLogSize"]
        set(value) {
            config["maxLogSize"] = value
            saveConfig()
        }

    var forceMountData: Boolean
        get() = config["forceMountData"]
        set(value) {
            config["forceMountData"] = value
            saveConfig()
        }

    var scopes: Map<String, ScopeConfig>
        get() = config["scope"]
        set(value) {
            config["scope"] = value
            saveConfig()
        }

    var templates: Map<String, TemplateConfig>
        get() = config["templates"]
        set(value) {
            config["templates"] = value
            saveConfig()
        }

    data class ScopeConfig(
        val aggressiveFilter: Boolean,
        val useWhitelist: Boolean,
        val excludeSystemApps: Boolean,
        val applyTemplates: Set<String>,
        val extraAppList: Set<String>
    )

    data class TemplateConfig(
        val isWhitelist: Boolean,
        val appList: Set<String>
    )

    @Suppress("UNCHECKED_CAST")
    private class JsonConfig(private val delegate: Any) {
        private val reflect = delegate.javaClass.reflect

        operator fun <T> get(field: String): T {
            return reflect.field(field)!!.get<T>(delegate)
        }

        operator fun <T> set(field: String, value: T) {
            return reflect.field(field)!!.put(delegate, value)
        }
    }
}