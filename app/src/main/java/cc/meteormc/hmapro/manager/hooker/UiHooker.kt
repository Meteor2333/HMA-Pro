package cc.meteormc.hmapro.manager.hooker

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.ManagerEntry
import cc.meteormc.hmapro.manager.ui.util.restart
import cc.meteormc.xposedkit.XposedKit
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.new
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import cc.meteormc.xposedkit.support.RuntimeXmlResource
import cc.meteormc.xposedkit.util.newSamProxy

object UiHooker : BaseHooker<HookerContext>() {
    private val preferenceItems = listOf(
        PreferenceItem(
            "modern_ui",
            R.drawable.modern_ui,
            R.string.modern_ui_title,
            null
        ),
        PreferenceItem(
            "fix_update_checker",
            R.drawable.fix_update_checker,
            R.string.fix_update_checker_title,
            null
        ),
        PreferenceItem(
            "full_backup",
            R.drawable.full_backup,
            R.string.full_backup_title,
            null
        )
    )

    override fun HookerContext.hook() {
        // todo: 第一个方案
//        XposedKit.registerAppAttachListener(HmaProModule.HMA_PACKAGE) {
//            val settingsId = it.findResource("settings", "xml") { _, id -> id }
//            Resources::class.reflect {
//                method("getXml", Int::class.javaPrimitiveType!!)?.hookAfter { info ->
//                    if (info.args[0] != settingsId) {
//                        return@hookAfter
//                    }
//
//                    info.result = buildXmlResource(it, info.result())
//                }
//            }
//        }

        //todo 第二个方案
//        DexMethods.SettingsPreferenceFragment_onCreate.hookAfter {
//            val fragment = it.instance ?: return@hookAfter
//            val context = DexMethods.Fragment_getContext.call<Context>(fragment)
//            val category = "androidx.preference.PreferenceCategory".reflect {
//                constructor(
//                    Context::class.java,
//                    AttributeSet::class.java
//                )!!.new<Any>(
//                    context,
//                    RuntimeXmlResource.create {
//                        name = "PreferenceCategory"
//                        attribute {
//                            namespace = RuntimeXmlResource.ANDROID_NS
//                            name = "title"
//                            value = "HMA-Pro"
//                            resolve(context)
//                        }
//                    }
//                )
//            }!!
//            "androidx.preference.SwitchPreference".reflect {
//                val ctor = constructor(
//                    Context::class.java,
//                    AttributeSet::class.java
//                )!!
//                for (item in preferenceItems) {
//                    val preference = ctor.new<Any>(
//                        context,
//                        RuntimeXmlResource.create {
//                            name = "SwitchPreference"
//                        }
//                    )
//                    addPreference(category, preference)
//                }
//            }!!
//        }

        // todo: 临时的测试方案
        DexMethods.SettingsPreferenceFragment_onCreate.hookAfter {
            val fragment = it.instance ?: return@hookAfter
            val context = DexMethods.Fragment_getContext.call<Context>(fragment)
            val resources = with(context.resources) {
                XposedKit.createModuleResources(displayMetrics, configuration, assets)
            }

            val manager = fragment.javaClass.reflect {
                field("a0")!!.get<Any>(fragment)
            }
            val root = manager.javaClass.reflect {
                field("h")!!.get<Any>(manager)
            }
            val list = root.javaClass.reflect {
                field("Q")!!.get<MutableList<Any>>(root)
            }
            val category = "androidx.preference.PreferenceCategory".reflect {
                val ddd = constructor(
                    Context::class.java,
                    AttributeSet::class.java
                )!!.new<Any>(
                    context,
                    RuntimeXmlResource.create {
                        name = "PreferenceCategory"
                    }
                )
                field("j")!!.put(ddd, resources.getString(R.string.app_name))
                ddd
            }!!
            "androidx.preference.SwitchPreference".reflect {
                val ctor = constructor(
                    Context::class.java,
                    AttributeSet::class.java
                )!!
                for (item in preferenceItems) {
                    val preference = ctor.new<Any>(
                        context,
                        RuntimeXmlResource.create {
                            name = "SwitchPreference"
                        }
                    )
                    field("m")!!.put(preference, resources.getDrawable(item.icon, null))
                    field("j")!!.put(preference, resources.getString(item.title))
                    field("k")!!.put(preference, item.summary?.let { s -> resources.getString(s) }.orEmpty())
                    field("g")!!.put(
                        preference,
                        newSamProxy(
                            classLoader,
                            "bm0".clazz!!,
                            "a"
                        ) { info ->
                            val preferences = ManagerEntry.preferences
                            preferences.modernUi = !preferences.modernUi
                            val activity = fragment.javaClass.reflect {
                                method("N")!!.call<Activity>(fragment)
                            }
                            activity.restart()
                            info.result = true
                        }
                    )
                    addPreference(category, preference)
                }
            }!!
            list.add(category)
        }
    }

//    private fun buildXmlResource(context: Context, base: XmlResourceParser): XmlResourceParser {
//        XLog.d(tag, "Building xml resource")
//        return RuntimeXmlResource.create {
//            fromXMLParser(context, base)
//
//        }
//    }

    private fun HookerContext.addPreference(
        preferenceGroup: Any,
        preference: Any
    ) {
        val preferences = "androidx.preference.PreferenceGroup".reflect {
            field(ArrayList::class.java)
        }!!.get<MutableList<Any>>(preferenceGroup)
        preferences.add(preference)
    }

    private class PreferenceItem(
        val key: String,
        @DrawableRes
        val icon: Int,
        @StringRes
        val title: Int,
        @StringRes
        val summary: Int?
    )
}