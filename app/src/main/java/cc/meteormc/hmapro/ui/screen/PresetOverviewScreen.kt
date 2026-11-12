package cc.meteormc.hmapro.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.ui.weight.BackButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar

@Composable
fun PresetOverviewScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceFromModule(R.string.preset_overview),
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->

    }
}