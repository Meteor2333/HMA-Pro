package cc.meteormc.hmapro.manager.hooker

import android.content.Context
import android.util.AttributeSet
import cc.meteormc.hmapro.DexMethods
import cc.meteormc.xposedkit.call
import cc.meteormc.xposedkit.get
import cc.meteormc.xposedkit.hook.BaseHooker
import cc.meteormc.xposedkit.hook.HookerContext
import cc.meteormc.xposedkit.support.RuntimeXmlResource

object UiHooker : BaseHooker<HookerContext>() {
    private val preferenceItems = listOf<Any>(

    )

    override fun HookerContext.hook() {
        DexMethods.SettingsPreferenceFragment_onCreate.hookAfter {
            val fragment = it.instance ?: return@hookAfter
            val context = DexMethods.Fragment_getContext.call<Context>(fragment)
            val category = "androidx.preference.PreferenceCategory".reflect {
                constructor(
                    Context::class.java,
                    AttributeSet::class.java
                )!!.call<Any>(
                    context,
                    RuntimeXmlResource.create {
                        name = "PreferenceCategory"
                        attribute {
                            namespace = RuntimeXmlResource.ANDROID_NS
                            name = "title"
                            value = "HMA-Pro"
                            resolve(context)
                        }
                    }
                )
            }!!
            "androidx.preference.SwitchPreference".reflect {
                val ctor = constructor(
                    Context::class.java,
                    AttributeSet::class.java
                )!!
                for (item in preferenceItems) {
                    val preference = ctor.call<Any>(
                        context,
                        RuntimeXmlResource.create {
                            name = "SwitchPreference"
                            //todo
                        }
                    )
                    addPreference(category, preference)
                }
            }!!

        }
    }

    private fun HookerContext.addPreference(
        preferenceGroup: Any,
        preference: Any
    ) {
        val preferences = "androidx.preference.PreferenceGroup".reflect {
            field(ArrayList::class.java)
        }!!.get<MutableList<Any>>(preferenceGroup)
        preferences.add(preference)
    }
}