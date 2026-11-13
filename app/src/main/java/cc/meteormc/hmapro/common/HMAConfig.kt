package cc.meteormc.hmapro.common

import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect

class HMAConfig(config: Any) {
    private val delegate = FieldAccessor(config)

    var detailLog: Boolean
        get() = delegate["detailLog"]
        set(value) { delegate["detailLog"] = value }

    var maxLogSize: Int
        get() = delegate["maxLogSize"]
        set(value) { delegate["maxLogSize"] = value }

    var forceMountData: Boolean
        get() = delegate["forceMountData"]
        set(value) { delegate["forceMountData"] = value }

    var scopes: Map<String, ScopeConfig>
        get() = delegate.get<Map<String, ScopeConfig>>("scope").mapValues { ScopeConfig(it.value) }
        set(value) { delegate["scope"] = value.mapValues { it.value.config } }

    var templates: Map<String, TemplateConfig>
        get() = delegate.get<Map<String, TemplateConfig>>("templates").mapValues { TemplateConfig(it.value) }
        set(value) { delegate["templates"] = value.mapValues { it.value.config } }

    class ScopeConfig(internal val config: Any) {
        private val delegate = FieldAccessor(config)

        fun init() {
            aggressiveFilter = false
            useWhitelist = false
            excludeSystemApps = true
            applyTemplates = mutableSetOf()
            extraAppList = mutableSetOf()
        }

        var aggressiveFilter: Boolean
            get() = delegate["aggressiveFilter"]
            set(value) { delegate["aggressiveFilter"] = value }

        var useWhitelist: Boolean
            get() = delegate["useWhitelist"]
            set(value) { delegate["useWhitelist"] = value }

        var excludeSystemApps: Boolean
            get() = delegate["excludeSystemApps"]
            set(value) { delegate["excludeSystemApps"] = value }

        var applyTemplates: MutableSet<String>
            get() = delegate["applyTemplates"]
            set(value) { delegate["applyTemplates"] = value }

        var extraAppList: MutableSet<String>
            get() = delegate["extraAppList"]
            set(value) { delegate["extraAppList"] = value }
    }

    class TemplateConfig(internal val config: Any) {
        private val delegate = FieldAccessor(config)

        fun init() {
            isWhitelist = false
            appList = emptySet()
        }

        var isWhitelist: Boolean
            get() = delegate["isWhitelist"]
            private set(value) { delegate["isWhitelist"] = value }

        var appList: Set<String>
            get() = delegate["appList"]
            private set(value) { delegate["appList"] = value }
    }

    private class FieldAccessor(private val instance: Any) {
        private val reflect = instance.javaClass.reflect

        operator fun <T> get(field: String): T {
            return reflect.field(field)!!.get<T>(instance)
        }

        operator fun <T> set(field: String, value: T) {
            return reflect.field(field)!!.put(instance, value)
        }
    }
}