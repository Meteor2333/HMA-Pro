package cc.meteormc.hmapro.manager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.xposedkit.get
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import java.util.Locale

@Composable
fun SettingsPage() {
    val prefs = LocalPreferencesState.current
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = MiuixScrollBehavior()

    val locales = DexFields.LangList_LOCALES.get<Array<String>>(null)
    val colorModes = arrayOf(
        ColorSchemeMode.System,
        ColorSchemeMode.Light,
        ColorSchemeMode.Dark
    )

    fun isMonet(mode: ColorSchemeMode): Boolean {
        return when (mode) {
            ColorSchemeMode.MonetSystem,
            ColorSchemeMode.MonetLight,
            ColorSchemeMode.MonetDark -> true
            else -> false
        }
    }

    fun useMonet(mode: ColorSchemeMode, useMonet: Boolean): ColorSchemeMode {
        return when (mode) {
            ColorSchemeMode.System, ColorSchemeMode.MonetSystem -> {
                if (useMonet) ColorSchemeMode.MonetSystem
                else ColorSchemeMode.System
            }
            ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> {
                if (useMonet) ColorSchemeMode.MonetLight
                else ColorSchemeMode.Light
            }
            ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> {
                if (useMonet) ColorSchemeMode.MonetDark
                else ColorSchemeMode.Dark
            }
        }
    }

    val languageOptions = locales.map {
            if (it == "SYSTEM") {
                stringResourceBy("follow_system")
            } else {
                val locale = Locale.forLanguageTag(it)
                locale.getDisplayName(locale)
            }
        }
    val themeOptions = listOf(
        stringResourceBy("follow_system"),
        stringResourceFromModule(R.string.theme_light),
        stringResourceFromModule(R.string.theme_dark)
    )
//    val logSizeOptions = mapOf(
//        128 to "128KiB",
//        256 to "256KiB",
//        512 to "512KiB",
//        1024 to "1MiB"
//    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceBy("title_settings"),
                scrollBehavior = scrollBehavior,
                defaultWindowInsetsPadding = false
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScrollEffect(scrollBehavior),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            item("language") {
                SettingsCard(title = stringResourceBy("settings_language")) {
                    OverlayDropdownPreference(
                        title = stringResourceBy("settings_language"),
                        items = languageOptions.toList(),
                        selectedIndex = locales.indexOfFirst { it == prefs.language },
                        onSelectedIndexChange = { prefs.language = locales[it] }
                    )

                    ArrowPreference(
                        title = stringResourceBy("settings_translate"),
                        summary = stringResourceBy("settings_translate_summary", stringResourceBy("app_name")),
                        onClick = {
                            //todo

                            uriHandler.openUri("https://crowdin.com/project/hide-my-applist")
                        }
                    )
                }
            }

            item("theme") {
                SettingsCard(title = stringResourceBy("settings_theme")) {
                    OverlayDropdownPreference(
                        title = stringResourceBy("settings_theme"),
                        items = themeOptions.toList(),
                        selectedIndex = colorModes.indexOfFirst { it == useMonet(prefs.colorMode, false) },
                        onSelectedIndexChange = { prefs.colorMode = useMonet(colorModes[it], isMonet(prefs.colorMode)) }
                    )

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.use_monet),
                        checked = isMonet(prefs.colorMode),
                        onCheckedChange = { prefs.colorMode = useMonet(prefs.colorMode, it) }
                    )

                    AnimatedVisibility(
                        visible = isMonet(prefs.colorMode)
                    ) {
                        Column {
                            Column {
                                OverlayDropdownPreference(
                                    title = stringResourceFromModule(R.string.color_spec_title),
                                    summary = stringResourceFromModule(R.string.color_spec_summary),
                                    items = ThemeColorSpec.entries.map { it.name },
                                    selectedIndex = ThemeColorSpec.entries.indexOfFirst { it == prefs.colorSpec },
                                    onSelectedIndexChange = { prefs.colorSpec = ThemeColorSpec.entries[it] }
                                )

                                OverlayDropdownPreference(
                                    title = stringResourceFromModule(R.string.palette_style_title),
                                    summary = stringResourceFromModule(R.string.palette_style_summary),
                                    items = ThemePaletteStyle.entries.map { it.name },
                                    selectedIndex = ThemePaletteStyle.entries.indexOfFirst { it == prefs.paletteStyle },
                                    onSelectedIndexChange = { prefs.paletteStyle = ThemePaletteStyle.entries[it] }
                                )
                            }
                        }
                    }

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.enable_blur_title),
                        checked = prefs.enableBlur,
                        onCheckedChange = { prefs.enableBlur = it }
                    )

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.enable_swipe_back_gesture_title),
                        summary = stringResourceFromModule(R.string.enable_swipe_back_gesture_summary),
                        checked = prefs.enableSwipeDismiss,
                        onCheckedChange = { prefs.enableSwipeDismiss = it }
                    )
                }
            }

            item("module") {
                SettingsCard(title = stringResourceBy("settings_module")) {
                    // todo
//                    OverlayDropdownPreference(
//                        title = stringResourceBy("settings_max_log_size"),
//                        items = logSizeOptions.values.toList(),
//                        selectedIndex = prefs.maxLogSize,
//                        onSelectedIndexChange = { prefs.maxLogSize = it }
//                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_hide_icon"),
                        summary = stringResourceBy("settings_hide_icon_summary"),
                        checked = prefs.hideIcon,
                        onCheckedChange = { prefs.hideIcon = it }
                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_disable_update"),
                        checked = prefs.disableUpdate,
                        onCheckedChange = { prefs.disableUpdate = it }
                    )
                }
            }

            item("service") {
                SettingsCard(title = stringResourceBy("settings_service")) {

                }
            }

            item("backup_and_restore") {
                SettingsCard(title = stringResourceBy("home_backup_and_restore")) {

                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    SmallTitle(
        text = title,
        insideMargin = PaddingValues(16.dp, 8.dp)
    )

    Card(content = content)

    Spacer(modifier = Modifier.height(8.dp))
}