package cc.meteormc.hmapro.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import cc.meteormc.hmapro.ui.page.HomePage
import cc.meteormc.hmapro.ui.page.LogsPage
import cc.meteormc.hmapro.ui.page.SettingsPage
import cc.meteormc.hmapro.ui.util.stringResourceBy
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Settings

@Composable
fun MainScreen() {
    val backdrop = rememberLayerBackdrop()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val pages = listOf(
        NavigationPage(
            label = stringResourceBy("title_home"),
            icon = MiuixIcons.Home,
            content = { HomePage() }
        ),
        NavigationPage(
            label = stringResourceBy("title_logs"),
            icon = MiuixIcons.Notes,
            content = { LogsPage() }
        ),
        NavigationPage(
            label = stringResourceBy("title_settings"),
            icon = MiuixIcons.Settings,
            content = { SettingsPage() }
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape
                ),
                mode = NavigationBarDisplayMode.IconWithSelectedLabel
            ) {
                pages.forEachIndexed { index, page ->
                    NavigationBarItem(
                        label = page.label,
                        icon = page.icon,
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selectedIndex,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { width -> width }
                            ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { width -> -width }
                            ) + fadeOut(tween(300))

                        } else {
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { width -> -width }
                            ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { width -> width }
                            ) + fadeOut(tween(300))
                        }
                    }
                ) { index ->
                    pages[index].content()
                }
            }
        }
    )
}

private data class NavigationPage(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)