package cc.meteormc.hmapro.manager.ui.screen

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar

@Composable
fun SharedTransitionScope.PresetOverviewScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .containerTransform("preset_overview"),
        topBar = {
            SmallTopAppBar(
                title = stringResourceFromModule(R.string.preset_overview),
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->

    }
}