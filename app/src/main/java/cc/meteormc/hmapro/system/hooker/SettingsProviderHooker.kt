package cc.meteormc.hmapro.system.hooker

import android.content.ContentProvider
import android.provider.Settings
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.hook.InvokeInfo
import cc.meteormc.xposedkit.put

object SettingsProviderHooker : BaseHooker<HookerContext>() {
    private val hiddenItems = mapOf(
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED to "0",
        Settings.Global.ADB_ENABLED to "0",
        "adb_wifi_enabled" to "0",
    )

    override fun HookerContext.hook() {
        "com.android.providers.settings.SettingsProvider".reflect {
            XLog.d(tag, "hook SettingsProvider")

            method("getGlobalSetting")?.hookAfter {
                handleSettings(it)
            }

            method("getSecureSetting")?.hookAfter {
                handleSettings(it)
            }

            method("getSystemSetting")?.hookAfter {
                handleSettings(it)
            }
        }
    }

    private fun HookerContext.handleSettings(info: InvokeInfo) {
        val name = info.findArg<String>()
        if (!hiddenItems.contains(name)) {
            return
        }

        val result = info.result ?: return
        val provider = info.instance<ContentProvider>()
        val caller = provider.callingPackage
        XLog.d(tag, "name: $name, result: $result, caller: $caller")

        $$"com.android.providers.settings.SettingsState$Setting".reflect {
            if (!type.isInstance(result)) return
            field("value")?.put(result, hiddenItems[name])
        }
    }
}