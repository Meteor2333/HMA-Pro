package cc.meteormc.hmapro.manager.ui.preference

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Stable
class PreferencesState(private val prefs: SharedPreferences) {
    var language by stringPreferenceOf("language", "SYSTEM")
    var colorMode by enumPreferenceOf("hmapro_color_mode", ColorSchemeMode.MonetSystem)
    var colorSpec by enumPreferenceOf("hmapro_color_spec", ThemeColorSpec.Spec2021)
    var paletteStyle by enumPreferenceOf("hmapro_palette_style", ThemePaletteStyle.TonalSpot)
    var enableBlur by boolPreferenceOf("hmapro_enable_blur", isRuntimeShaderSupported())
    var enableSwipeDismiss by boolPreferenceOf("hmapro_enable_swipe_dismiss", true)
    var hideIcon by boolPreferenceOf("hide_icon", false)
    var disableUpdate by boolPreferenceOf("disable_update", false)
    var logFilter by intPreferenceOf("log_filter_level", 0)

    private inline fun <T> preferenceOf(
        provider: () -> T,
        noinline writer: (T) -> Unit
    ): MutableState<T> {
        return PreferenceState(provider(), writer)
    }

    private fun intPreferenceOf(key: String, defValue: Int): MutableState<Int> {
        return preferenceOf(
            provider = { prefs.getInt(key, defValue) },
            writer = { prefs.edit { putInt(key, it) } }
        )
    }

    private fun stringPreferenceOf(key: String, defValue: String): MutableState<String> {
        return preferenceOf(
            provider = { prefs.getString(key, defValue)!! },
            writer = { prefs.edit { putString(key, it) } }
        )
    }

    private fun boolPreferenceOf(key: String, defValue: Boolean): MutableState<Boolean> {
        return preferenceOf(
            provider = { prefs.getBoolean(key, defValue) },
            writer = { prefs.edit { putBoolean(key, it) } }
        )
    }

    private inline fun <reified T : Enum<T>> enumPreferenceOf(key: String, defValue: T): MutableState<T> {
        return preferenceOf(
            provider = {
                val name = prefs.getString(key, null)
                if (name != null) enumValueOf<T>(name)
                else defValue
            },
            writer = { prefs.edit { putString(key, it.name) } }
        )
    }

    private class PreferenceState<T>(
        initialValue: T,
        private val writer: (T) -> Unit
    ) : MutableState<T> {
        private var _value by mutableStateOf(initialValue)

        override var value: T
            get() = _value
            set(newValue) {
                _value = newValue
                writer(newValue)
            }

        override fun component1(): T = value

        override fun component2(): (T) -> Unit = { value = it }
    }
}