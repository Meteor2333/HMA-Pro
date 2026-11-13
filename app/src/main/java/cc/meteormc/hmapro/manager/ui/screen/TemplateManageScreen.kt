package cc.meteormc.hmapro.manager.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import cc.meteormc.hmapro.manager.ui.weight.TipCard
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TemplateListScreen() {
    val navigator = LocalNavigator.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_template_manage"),
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.push(Route.TemplateSettings)
                }
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "Add Template",
                    modifier = Modifier.size(32.dp),
                    tint = MiuixTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        val templates by remember { mutableStateOf(emptyList<HMAConfig.TemplateConfig>()) }
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(
                items = templates,
                key = { it.toString() }
            ) {

            }
        }
    }
}

@Composable
fun TemplateSettingsScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_template_settings"),
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->
        TipCard(stringResourceBy("template_usage_hint"))
    }
}