package cc.meteormc.hmapro.common

import android.provider.Settings
import androidx.annotation.StringRes
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.xposedkit.allocate
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import cc.meteormc.xposedkit.util.extrasOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.EnumSet

class HMAConfig(internal val config: Any) {
    companion object {
        const val EXTRA_RAW_JSON = "rawJson"
        const val EXTRA_HIDDEN_SETTINGS = "hiddenSettings"
    }

    private val extras = extrasOf(config)
    private val delegate = FieldAccessor(config)

    var rawJson: JSONObject
        get() = extras[EXTRA_RAW_JSON] ?: JSONObject()
        private set(value) { extras[EXTRA_RAW_JSON] = value }

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

    fun readExtras(json: JSONObject) {
        rawJson = json

        json.optJSONObject("scope")?.apply {
            for ((packageName, scope) in scopes) {
                val obj = optJSONObject(packageName) ?: continue
                scope.readExtras(obj)
            }
        }

        json.optJSONObject("templates")?.apply {
            for ((name, template) in templates) {
                val obj = optJSONObject(name) ?: continue
                template.readExtras(obj)
            }
        }
    }

    fun writeExtras(json: JSONObject) {
        json.optJSONObject("scope")?.apply {
            for ((packageName, scope) in scopes) {
                val obj = optJSONObject(packageName) ?: continue
                scope.writeExtras(obj)
            }
        }

        json.optJSONObject("templates")?.apply {
            for ((name, template) in templates) {
                val obj = optJSONObject(name) ?: continue
                template.writeExtras(obj)
            }
        }
    }

    class ScopeConfig(internal val config: Any) {
        private val extras = extrasOf(config)
        private val delegate = FieldAccessor(config)

        companion object {
            fun new(): ScopeConfig {
                return ManagerMain.classLoader.reflect($$"icu.nullptr.hidemyapplist.common.JsonConfig$AppConfig") {
                    ScopeConfig(type.allocate())
                }!!.apply {
                    aggressiveFilter = false
                    useWhitelist = false
                    excludeSystemApps = true
                    applyTemplates = mutableSetOf()
                    extraAppList = mutableSetOf()
                }
            }
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

        var applyTemplates: Set<String>
            get() = delegate["applyTemplates"]
            set(value) { delegate["applyTemplates"] = value }

        var extraAppList: Set<String>
            get() = delegate["extraAppList"]
            set(value) { delegate["extraAppList"] = value }

        var hiddenSettings: Set<HiddenSettings>
            get() = extras[EXTRA_HIDDEN_SETTINGS] ?: emptySet()
            set(value) { extras[EXTRA_HIDDEN_SETTINGS] = value }

        fun readExtras(json: JSONObject) {
            json.optJSONArray(EXTRA_HIDDEN_SETTINGS)?.apply {
                val settings = EnumSet.noneOf(HiddenSettings::class.java)
                for (i in 0 until length()) {
                    val item = try {
                        val str = getString(i) ?: continue
                        HiddenSettings.valueOf(str)
                    } catch (_: Throwable) {
                        continue
                    }

                    settings.add(item)
                }
                hiddenSettings = settings
            }
        }

        fun writeExtras(json: JSONObject) {
            JSONArray().apply {
                for (setting in hiddenSettings) { put(setting.name) }
                json.put(EXTRA_HIDDEN_SETTINGS, this)
            }
        }
    }

    class TemplateConfig(internal val config: Any) {
        private val extras = extrasOf(config)
        private val delegate = FieldAccessor(config)

        companion object {
            fun new(): TemplateConfig {
                return ManagerMain.classLoader.reflect($$"icu.nullptr.hidemyapplist.common.JsonConfig$Template") {
                    TemplateConfig(type.allocate())
                }!!.apply {
                    isWhitelist = false
                    appList = emptySet()
                }
            }
        }

        var isWhitelist: Boolean
            get() = delegate["isWhitelist"]
            set(value) { delegate["isWhitelist"] = value }

        var appList: Set<String>
            get() = delegate["appList"]
            set(value) { delegate["appList"] = value }

        fun readExtras(json: JSONObject) {

        }

        fun writeExtras(json: JSONObject) {

        }
    }

    enum class HiddenSettings(
        @StringRes
        val message: Int,
        vararg kv: Pair<String, String>
    ) {
        DEVELOPER_OPTIONS(
            R.string.developer_options,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED to "0"
        ),
        ADB_DEBUGGING(
            R.string.adb_debugging,
            Settings.Global.ADB_ENABLED to "0",
            "adb_wifi_enabled" to "0"
        ),
        ACCESSIBILITY(
            R.string.accessibility,
            Settings.Secure.ACCESSIBILITY_ENABLED to "0",
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES to ""
        );

        val items = kv.toMap()
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