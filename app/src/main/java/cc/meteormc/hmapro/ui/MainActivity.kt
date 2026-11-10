package cc.meteormc.hmapro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import cc.meteormc.hmapro.data.HMAVersion
import cc.meteormc.hmapro.data.ModuleStatus
import cc.meteormc.hmapro.ui.nav.Route
import cc.meteormc.hmapro.ui.screen.AboutScreen
import cc.meteormc.hmapro.ui.screen.DevelopersScreen
import cc.meteormc.hmapro.ui.screen.MainScreen
import cc.meteormc.hmapro.ui.screen.OpenSourceLicensesScreen
import cc.meteormc.hmapro.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.ui.util.LocalModuleResources
import cc.meteormc.hmapro.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.ui.util.LocalNavigator
import cc.meteormc.xposedkit.XposedKit
import cc.meteormc.xposedkit.put
import cc.meteormc.xposedkit.reflect
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.rememberNavController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }

    override fun recreate() {
        javaClass.reflect {
            field("_viewModelStore")?.put(this@MainActivity, null)
        }

        super.recreate()
    }
}

@Composable
fun App() {
    val themeController = remember {
        ThemeController(ColorSchemeMode.MonetSystem)
    }

    val context = LocalContext.current
    val resources = LocalResources.current
    val hmaVersion = HMAVersion.current(context)
    val moduleResource = XposedKit.createModuleResources(copyFrom = resources.assets)
    val moduleStatus = ModuleStatus.current(context)
    val navigator = rememberNavController<Route>(Route.Main)

    MiuixTheme(controller = themeController) {
        CompositionLocalProvider(
            LocalHMAVersion provides hmaVersion,
            LocalModuleResources provides moduleResource,
            LocalModuleStatus provides moduleStatus,
            LocalNavigator provides navigator
        ) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.pop() }
            ) {
                entry<Route.Main> {
                    MainScreen()
                }

                entry<Route.About> {
                    AboutScreen()
                }

                entry<Route.Developers> {
                    DevelopersScreen()
                }

                entry<Route.OpenSourceLicenses> {
                    OpenSourceLicensesScreen()
                }
            }
        }
    }
}