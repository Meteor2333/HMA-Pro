package cc.meteormc.hmapro.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.ui.util.stringResourceBy
import cc.meteormc.hmapro.ui.util.verticalScrollEffect
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar

@Composable
fun SettingsPage() {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceBy("title_settings"),
                scrollBehavior = scrollBehavior
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScrollEffect(scrollBehavior),
                contentPadding = PaddingValues(12.dp)
            ) {

            }
        }
    )
}