package cc.meteormc.hmapro.manager.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.LayoutDirection
import cc.meteormc.hmapro.manager.data.HMAVersion
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.hooker.ServiceHooker
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.preference.PreferencesState
import cc.meteormc.hmapro.manager.ui.screen.AboutScreen
import cc.meteormc.hmapro.manager.ui.screen.AppListScreen
import cc.meteormc.hmapro.manager.ui.screen.AppSettingsScreen
import cc.meteormc.hmapro.manager.ui.screen.DevelopersScreen
import cc.meteormc.hmapro.manager.ui.screen.MainScreen
import cc.meteormc.hmapro.manager.ui.screen.OpenSourceLicensesScreen
import cc.meteormc.hmapro.manager.ui.screen.PresetOverviewScreen
import cc.meteormc.hmapro.manager.ui.screen.TemplateListScreen
import cc.meteormc.hmapro.manager.ui.screen.TemplateSettingsScreen
import cc.meteormc.hmapro.manager.ui.util.ContainerTransformState
import cc.meteormc.hmapro.manager.ui.util.LocalContainerTransformState
import cc.meteormc.hmapro.manager.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.manager.ui.util.LocalModuleResources
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.createResourceForModule
import org.json.JSONObject
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.rememberNavController
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContent { App(prefs) }
    }
}

@Composable
private fun App(preferences: SharedPreferences) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    val hmaVersion = HMAVersion.current(context)
    var moduleResource by remember {
        mutableStateOf(resources.createResourceForModule())
    }
    var moduleStatus by remember {
        mutableStateOf<ModuleStatus>(ModuleStatus.Disabled)
    }

    val containerTransformState = remember { ContainerTransformState() }
    val prefsState = remember { PreferencesState(preferences) }
    val navigator = rememberNavController<Route>(Route.Main)

    val themeController = remember(
        prefsState.colorMode,
        prefsState.colorSpec,
        prefsState.paletteStyle
    ) {
        ThemeController(
            colorSchemeMode = prefsState.colorMode,
            keyColor = readSystemSeedColor(context),
            colorSpec = prefsState.colorSpec,
            paletteStyle = prefsState.paletteStyle
        )
    }

    LaunchedEffect(prefsState.language) {
        val language = prefsState.language
        val config = resources.configuration
        @Suppress("UsePropertyAccessSyntax")
        config.setLocales(
            if (language == "SYSTEM") LocaleList.getEmptyLocaleList()
            else LocaleList.forLanguageTags(language)
        )
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        if (config.locales != moduleResource.configuration.locales) {
            moduleResource = resources.createResourceForModule()
        }
    }

    LaunchedEffect(Unit) {
        ServiceHooker.eventFlow.collect {
            moduleStatus = ModuleStatus.current(context)
        }
    }

    MiuixTheme(controller = themeController) {
        CompositionLocalProvider(
            LocalContainerTransformState provides containerTransformState,
            LocalHMAVersion provides hmaVersion,
            LocalModuleResources provides moduleResource,
            LocalModuleStatus provides moduleStatus,
            LocalNavigator provides navigator,
            LocalPreferencesState provides prefsState
        ) {
            val swipeDismiss = remember(prefsState.enableSwipeDismiss) {
                if (prefsState.enableSwipeDismiss) {
                    if (layoutDirection == LayoutDirection.Rtl) {
                        NavSwipeDirection.RightToLeft
                    } else {
                        NavSwipeDirection.LeftToRight
                    }
                } else {
                    NavSwipeDirection.None
                }
            }

            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.pop() }
            ) {
                entry<Route.Main>(
                    swipeDismiss = swipeDismiss
                ) {
                    MainScreen()
                }

                entry<Route.About>(
                    swipeDismiss = swipeDismiss
                ) {
                    AboutScreen()
                }

                entry<Route.Developers>(
                    swipeDismiss = swipeDismiss
                ) {
                    DevelopersScreen()
                }

                entry<Route.OpenSourceLicenses>(
                    swipeDismiss = swipeDismiss
                ) {
                    OpenSourceLicensesScreen()
                }

                entry<Route.AppList>(
                    transition = containerTransform(containerTransformState),
                    swipeDismiss = swipeDismiss
                ) {
                    AppListScreen()
                }

                entry<Route.AppSettings>(
                    swipeDismiss = swipeDismiss
                ) {
                    AppSettingsScreen()
                }

                entry<Route.TemplateList>(
                    transition = containerTransform(containerTransformState),
                    swipeDismiss = swipeDismiss
                ) {
                    TemplateListScreen()
                }

                entry<Route.TemplateSettings>(
                    swipeDismiss = swipeDismiss
                ) {
                    TemplateSettingsScreen()
                }

                entry<Route.PresetOverview>(
                    transition = containerTransform(containerTransformState),
                    swipeDismiss = swipeDismiss
                ) {
                    PresetOverviewScreen()
                }
            }
        }
    }
}

private fun readSystemSeedColor(context: Context): Color {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val json = Settings.Secure.getString(
            context.contentResolver,
            "theme_customization_overlay_packages",
        )?.let { JSONObject(it) }
        if (json != null) {
            val palette = json.optString("android.theme.customization.system_palette", "")
            val result = if (palette.isNotBlank()) {
                android.graphics.Color.parseColor(
                    if (palette.startsWith("#")) palette else "#$palette"
                )
            } else {
                context.resources.getColor(android.R.color.system_accent1_500, context.theme)
            }

            return Color(result)
        }
    }

    return Color(0xFF6750A4)
}