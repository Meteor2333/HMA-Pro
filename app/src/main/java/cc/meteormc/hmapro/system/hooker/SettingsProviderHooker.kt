package cc.meteormc.hmapro.system.hooker

import android.content.ContentProvider
import cc.meteormc.hmapro.system.SystemServerMain
import cc.meteormc.xposedkit.XLog
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.hook.InvokeInfo
import cc.meteormc.xposedkit.put

object SettingsProviderHooker : BaseHooker<HookerContext>() {
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
        // Settings.Global.DEVELOPMENT_SETTINGS_ENABLED to "0",
        // Settings.Global.ADB_ENABLED to "0",
        // "adb_wifi_enabled" to "0"
        val scopes = SystemServerMain.config?.scopes ?: return
        val provider = info.instance<ContentProvider>()
        val caller = provider.callingPackage

        val hiddenSettings = scopes[caller]?.hiddenSettings ?: return
        val name = info.findArg<String>()
        if (name in hiddenSettings) {
            val result = info.result ?: return
            $$"com.android.providers.settings.SettingsState$Setting".reflect {
                if (!type.isInstance(result)) return
                field("value")?.put(result, hiddenSettings[name])
            }
        }

        XLog.i(tag, "@shouldFilterSettings: query from $caller, target is $name")
    }
}