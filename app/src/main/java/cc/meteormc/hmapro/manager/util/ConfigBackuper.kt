package cc.meteormc.hmapro.manager.util

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object ConfigBackuper {
    private const val TAG = "ConfigBackuper"
    private const val EXTRA_PREFERENCES = "extra_preferences"

    suspend fun backup(context: Context, output: Uri) = withContext(Dispatchers.IO) {
        val config = ConfigManager.config.config.toString()
        val json = try {
            JSONObject(config)
        } catch (t: Throwable) {
            XLog.w(TAG, "Failed to parse config json!", t)
            return@withContext false
        }

        val preferences = JSONObject().apply {
            json.put(EXTRA_PREFERENCES, this)
        }
        ManagerMain.preferences
            .storage
            .all
            .entries
            .forEach {
                val key = it.key
                val value = it.value
                if (value is Collection<*>) {
                    preferences.put(key, JSONArray(value))
                } else {
                    preferences.put(key, value)
                }
            }

        context.contentResolver
            .openOutputStream(output)
            ?.bufferedWriter()
            .use {
                if (it == null) return@use false
                it.write(json.toString(2))
                true
            }
    }

    suspend fun restore(context: Context, input: Uri) = withContext(Dispatchers.IO) {
        val json = context.contentResolver
            .openInputStream(input)
            ?.reader()
            ?.use { it.readText() }
            ?.runCatching { JSONObject(this) }
            ?.onFailure { XLog.w(TAG, "Failed to parse config json!", it) }
            ?.getOrNull()
            ?: return@withContext false
        val config = try {
            val companion = DexFields.JsonConfig_Companion.get<Any>(null)
            val delegate = DexMethods.JsonConfig_parse.call<Any>(companion, json.toString())
            HMAConfig(delegate)
        } catch (t: Throwable) {
            XLog.e(TAG, "Failed to decode config!", t)
            return@withContext false
        }
        ConfigManager.config = config

        val map = mutableMapOf<String, Any>()
        val preferences = json.optJSONObject(EXTRA_PREFERENCES) ?: return@withContext true
        preferences.keys().forEach { map[it] = preferences.get(it) }
        ManagerMain.preferences.storage.edit {
            clear()
            map.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putFloat(key, value.toFloat())
                    is Boolean -> putBoolean(key, value)
                    is JSONArray -> putStringSet(
                        key,
                        List(value.length()) { i -> value.optString(i) }.toSet()
                    )
                }
            }
        }

        true
    }
}