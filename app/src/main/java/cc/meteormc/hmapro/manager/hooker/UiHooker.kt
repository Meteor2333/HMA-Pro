package cc.meteormc.hmapro.manager.hooker

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.meteormc.hmapro.DexClasses
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.Constants
import cc.meteormc.hmapro.manager.ManagerMain.preferences
import cc.meteormc.hmapro.manager.ui.util.findResource
import cc.meteormc.hmapro.manager.ui.util.restart
import cc.meteormc.xposedkit.XposedKit
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import cc.meteormc.xposedkit.support.RuntimeXmlResource
import cc.meteormc.xposedkit.util.newFunctionalProxy
import java.lang.ref.WeakReference

object UiHooker : BaseHooker<HookerContext>() {
    private var moduleResources: Resources? = null
    private val preferenceItems = listOf(
        PreferenceItem(
            key = "modern_ui",
            type = "SwitchPreference",
            icon = R.drawable.ic_modern_ui,
            title = R.string.modern_ui_title,
            summary = R.string.modern_ui_summary,
            onConfigure = {
                DexMethods.TwoStatePreference_setChecked.call(
                    preference, preferences.modernUi
                )
            },
            onChange = {
                preferences.modernUi = it as Boolean
                activity.restart()
                true
            }
        ),
        PreferenceItem(
            key = "fix_update_checker",
            type = "SwitchPreference",
            icon = R.drawable.ic_fix_update_checker,
            title = R.string.fix_update_checker_title,
            summary = R.string.fix_update_checker_summary,
            onConfigure = {
                DexMethods.TwoStatePreference_setChecked.call(
                    preference, preferences.fixUpdateChecker
                )
            },
            onChange = {
                preferences.fixUpdateChecker = it as Boolean
                true
            }
        ),
        PreferenceItem(
            key = "full_backup",
            type = "SwitchPreference",
            icon = R.drawable.ic_full_backup,
            title = R.string.full_backup_title,
            summary = R.string.full_backup_summary,
            onConfigure = {
                DexMethods.TwoStatePreference_setChecked.call(
                    preference, preferences.fullBackup
                )
            },
            onChange = {
                preferences.fullBackup = it as Boolean
                true
            }
        )
    )

    override fun HookerContext.hook() {
        XposedKit.registerAppAttachListener(Constants.HMA_PACKAGE) {
            val settingsId = it.findResource("settings", "xml") { _, id -> id }
            Resources::class.reflect {
                method("getXml", Int::class.javaPrimitiveType!!)?.hookAfter { info ->
                    if (info.args[0] != settingsId) {
                        return@hookAfter
                    }

                    info.result = buildXmlResource(it, info.result())
                }
            }
        }

        DexMethods.SettingsPreferenceFragment_onCreatePreferences.hookAfter {
            val fragment = WeakReference(it.instance ?: return@hookAfter)
            for (item in preferenceItems) {
                configurePreference(fragment, item)
            }
        }
    }

    private fun HookerContext.configurePreference(
        weakFragment: WeakReference<Any>,
        item: PreferenceItem
    ) {
        val fragment = weakFragment.get() ?: return
        val activity = DexMethods.Fragment_requireActivity.call<Activity>(fragment)
        val context = DexMethods.Fragment_requireContext.call<Context>(fragment)
        val preference = DexMethods.PreferenceFragmentCompat_findPreference.call<Any?>(fragment, item.key) ?: return

        val moduleResources = calcModuleResources(context.resources)
        val icon = moduleResources.getDrawable(item.icon, context.theme)

        val listener = newFunctionalProxy(
            classLoader,
            DexClasses.OnPreferenceChangeListener
        ) {
            val preference = it.args[0] ?: return@newFunctionalProxy
            val newValue = it.args[1] ?: return@newFunctionalProxy
            val fragment = weakFragment.get() ?: return@newFunctionalProxy
            val activity = DexMethods.Fragment_requireActivity.call<Activity>(fragment)
            val context = DexMethods.Fragment_requireContext.call<Context>(fragment)
            val event = EventContext(
                activity,
                context,
                preference,
                item
            )
            it.result = item.onChange(event, newValue)
        }

        "androidx.preference.Preference".reflect {
            field(Drawable::class.java)?.put(preference, icon)
            field(DexClasses.OnPreferenceChangeListener)?.put(preference, listener)
        }

        item.onConfigure(
            EventContext(
                activity,
                context,
                preference,
                item
            )
        )
    }

    private fun calcModuleResources(from: Resources): Resources {
        var cached = moduleResources
        if (cached == null || from.displayMetrics != cached.displayMetrics || from.configuration != cached.configuration) {
            cached = with(from) {
                XposedKit.createModuleResources(displayMetrics, configuration, assets)
            }
            moduleResources = cached
        }

        return cached
    }

    private fun buildXmlResource(context: Context, base: XmlResourceParser): XmlResourceParser {
        val moduleResources = calcModuleResources(context.resources)
        return RuntimeXmlResource.create {
            fromXMLParser(context, base)
            child {
                name = "PreferenceCategory"
                attribute {
                    namespace = RuntimeXmlResource.ANDROID_NS
                    name = "title"
                    value = moduleResources.getString(R.string.app_name)
                    valueData = 0
                    valueType = TypedValue.TYPE_STRING
                    resolve(context)
                }
                for (item in preferenceItems) {
                    child {
                        name = item.type
                        attribute {
                            namespace = RuntimeXmlResource.ANDROID_NS
                            name = "key"
                            value = item.key
                            valueData = 0
                            valueType = TypedValue.TYPE_STRING
                            resolve(context)
                        }
                        attribute {
                            namespace = RuntimeXmlResource.ANDROID_NS
                            name = "title"
                            value = moduleResources.getString(item.title)
                            valueData = 0
                            valueType = TypedValue.TYPE_STRING
                            resolve(context)
                        }
                        if (item.summary != null) {
                            attribute {
                                namespace = RuntimeXmlResource.ANDROID_NS
                                name = "summary"
                                value = moduleResources.getString(item.summary)
                                valueData = 0
                                valueType = TypedValue.TYPE_STRING
                                resolve(context)
                            }
                        }
                        attribute {
                            namespace = RuntimeXmlResource.ANDROID_NS
                            name = "persistent"
                            value = "false"
                            resolve(context)
                        }
                    }
                }
            }
        }
    }

    private class EventContext(
        val activity: Activity,
        val context: Context,
        val preference: Any,
        val item: PreferenceItem
    )

    private class PreferenceItem(
        val key: String,
        val type: String,
        @DrawableRes
        val icon: Int,
        @StringRes
        val title: Int,
        @StringRes
        val summary: Int? = null,
        val onConfigure: EventContext.() -> Unit = { },
        val onChange: EventContext.(newValue: Any) -> Boolean = { true }
    )
}