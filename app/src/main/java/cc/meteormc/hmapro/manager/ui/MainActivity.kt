package cc.meteormc.hmapro.manager.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import cc.meteormc.hmapro.manager.ManagerMain
import cc.meteormc.hmapro.manager.ManagerMain.preferences
import cc.meteormc.hmapro.manager.data.HMAVersion
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.ui.nav.NavMetadata
import cc.meteormc.hmapro.manager.ui.nav.NavMetadata.has
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.nav.rememberNavigator
import cc.meteormc.hmapro.manager.ui.preference.PreferencesState
import cc.meteormc.hmapro.manager.ui.screen.AboutScreen
import cc.meteormc.hmapro.manager.ui.screen.DevelopersScreen
import cc.meteormc.hmapro.manager.ui.screen.MainScreen
import cc.meteormc.hmapro.manager.ui.screen.OpenSourceLicensesScreen
import cc.meteormc.hmapro.manager.ui.screen.PresetOverviewScreen
import cc.meteormc.hmapro.manager.ui.screen.ScopeListScreen
import cc.meteormc.hmapro.manager.ui.screen.ScopeSettingsScreen
import cc.meteormc.hmapro.manager.ui.screen.TemplateListScreen
import cc.meteormc.hmapro.manager.ui.screen.TemplateSettingsScreen
import cc.meteormc.hmapro.manager.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.manager.ui.util.LocalModuleResources
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.xposedkit.XposedKit
import org.json.JSONObject
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        var newContext = newBase
        if (newBase != null) {
            val language = preferences.language
            val locales = if (language == "SYSTEM") {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(language)
            }

            val configuration = Configuration(newBase.resources.configuration)
            @Suppress("UsePropertyAccessSyntax")
            configuration.setLocales(locales)
            newContext = newBase.createConfigurationContext(configuration)
        }

        super.attachBaseContext(newContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        setContent {
            val isSystemInDark = isSystemInDarkTheme()
            DisposableEffect(preferences.colorMode, isSystemInDark) {
                val transparent = android.graphics.Color.TRANSPARENT
                val isDark = when (preferences.colorMode) {
                    ColorSchemeMode.System, ColorSchemeMode.MonetSystem -> isSystemInDark
                    ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> false
                    ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> true
                }
                val style = if (isDark) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }

                enableEdgeToEdge(style, style)
                onDispose { }
            }

            App(preferences)
        }
    }
}

@Composable
private fun App(preferences: PreferencesState) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val hmaVersion = HMAVersion.current(context)
    val moduleResource = remember(resources) {
        XposedKit.createModuleResources(
            metrics = resources.displayMetrics,
            config = resources.configuration,
            copyFrom = resources.assets
        )
    }
    var moduleStatus by remember {
        mutableStateOf<ModuleStatus>(ModuleStatus.Disabled)
    }

    val themeController = remember(
        preferences.colorMode,
        preferences.colorSpec,
        preferences.paletteStyle
    ) {
        ThemeController(
            colorSchemeMode = preferences.colorMode,
            keyColor = readSystemSeedColor(context),
            colorSpec = preferences.colorSpec,
            paletteStyle = preferences.paletteStyle
        )
    }

    LaunchedEffect(Unit) {
        ManagerMain.serviceBindEvent.collect {
            moduleStatus = ModuleStatus.current(context)
        }
    }

    val navigator = rememberNavigator(Route.Main)
    MiuixTheme(controller = themeController) {
        CompositionLocalProvider(
            LocalHMAVersion provides hmaVersion,
            LocalModuleResources provides moduleResource,
            LocalModuleStatus provides moduleStatus,
            LocalNavigator provides navigator,
            LocalPreferencesState provides preferences
        ) {
            SharedTransitionLayout {
                // todo: 尝试支持swipeDismiss（使用predictivePopTransitionSpec）
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.back() },
                    transitionSpec = {
                        if (targetState.metadata.has(NavMetadata.DISABLE_TRANSITION)) {
                            return@NavDisplay EnterTransition.None togetherWith ExitTransition.None
                        }

                        slideInHorizontally(
                            animationSpec = tween(500),
                            initialOffsetX = { width -> width }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(500),
                            targetOffsetX = { width -> -width / 4 }
                        ) + fadeOut(
                            animationSpec = tween(500, easing = LinearEasing),
                            targetAlpha = 0.5f
                        )
                    },
                    popTransitionSpec = {
                        if (initialState.metadata.has(NavMetadata.DISABLE_TRANSITION)) {
                            return@NavDisplay EnterTransition.None togetherWith ExitTransition.None
                        }

                        slideInHorizontally(
                            animationSpec = tween(500),
                            initialOffsetX = { width -> -width / 4 }
                        ) + fadeIn(
                            animationSpec = tween(500, easing = LinearEasing),
                            initialAlpha = 0.5f
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(500),
                            targetOffsetX = { width -> width }
                        )
                    },
                    entryProvider = entryProvider {
                        entry<Route.Main> {
                            MainScreen()
                        }

                        entry<Route.About>(
                            metadata = NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            AboutScreen()
                        }

                        entry<Route.Developers>(
                            metadata = NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            DevelopersScreen()
                        }

                        entry<Route.OpenSourceLicenses>(
                            metadata = NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            OpenSourceLicensesScreen()
                        }

                        entry<Route.ScopeList>(
                            metadata = NavMetadata.DISABLE_TRANSITION + NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            ScopeListScreen()
                        }

                        entry<Route.ScopeSettings> {
                            ScopeSettingsScreen(it.title, it.packageName)
                        }

                        entry<Route.TemplateList>(
                            metadata = NavMetadata.DISABLE_TRANSITION + NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            TemplateListScreen()
                        }

                        entry<Route.TemplateSettings> {
                            TemplateSettingsScreen()
                        }

                        entry<Route.PresetOverview>(
                            metadata = NavMetadata.DISABLE_TRANSITION + NavMetadata.ENABLE_SWIPE_DISMISS
                        ) {
                            PresetOverviewScreen()
                        }
                    }
                )
            }
        }
    }
}

private fun readSystemSeedColor(context: Context): Color {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val json = Settings.Secure.getString(
            context.contentResolver,
            "theme_customization_overlay_packages"
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