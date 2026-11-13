package cc.meteormc.hmapro.manager.service

import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get

object ConfigManager {
    private val config by lazy {
        val cm = DexFields.ConfigManager_INSTANCE.get<Any>(null)
        val delegate = DexFields.ConfigManager_config.get<Any?>(cm)
            ?: throw IllegalStateException("ConfigManager.config is null")
        return@lazy HMAConfig(delegate)
    }

    private fun saveConfig() {
        DexMethods.ConfigManager_saveConfig.call<Unit>(config)
    }

    var detailLog: Boolean
        get() = config.detailLog
        set(value) {
            config.detailLog = value
            saveConfig()
        }

    var maxLogSize: Int
        get() = config.maxLogSize
        set(value) {
            config.maxLogSize = value
            saveConfig()
        }

    var forceMountData: Boolean
        get() = config.forceMountData
        set(value) {
            config.forceMountData = value
            saveConfig()
        }

    var scopes: Map<String, HMAConfig.ScopeConfig>
        get() = config.scopes
        set(value) {
            config.scopes = value
            saveConfig()
        }

    var templates: Map<String, HMAConfig.TemplateConfig>
        get() = config.templates
        set(value) {
            config.templates = value
            saveConfig()
        }
}