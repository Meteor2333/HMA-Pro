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
        val scopes = SystemServerMain.config?.scopes ?: return
        val provider = info.instance<ContentProvider>()
        val caller = provider.callingPackage

        val name = info.findArg<String>()
        val hiddenSettings = scopes[caller]?.hiddenSettings ?: return
        val newValue = hiddenSettings.firstNotNullOfOrNull { it.items[name] } ?: return

        val result = info.result ?: return
        XLog.i(tag, "@shouldFilterSettings: query from $caller, target is $name")
        $$"com.android.providers.settings.SettingsState$Setting".reflect {
            if (!type.isInstance(result)) return
            field("value")?.put(result, newValue)
        }
    }
}