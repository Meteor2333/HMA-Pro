package cc.meteormc.hmapro.manager.service

import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.put

object ConfigManager {
    private lateinit var _config: HMAConfig
    internal var config: HMAConfig
        get() {
            if (!::_config.isInitialized) {
                val cm = DexFields.ConfigManager_INSTANCE.get<Any>(null)
                val delegate = DexFields.ConfigManager_config.get<Any?>(cm)
                    ?: throw IllegalStateException("ConfigManager.config is null")
                _config = HMAConfig(delegate)
            }

            return _config
        }
        set(value) {
            _config = value
            val cm = DexFields.ConfigManager_INSTANCE.get<Any>(null)
            DexFields.ConfigManager_config.put(cm, value.config)
            saveConfig()
        }

    fun saveConfig() {
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