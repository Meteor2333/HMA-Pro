package cc.meteormc.hmapro.manager.ui.screen

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.common.util.PresetResolver.resolveApps
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.data.InstalledPackage.Companion.toInstallPackage
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.component.AppIcon
import cc.meteormc.hmapro.manager.ui.component.EmptyContent
import cc.meteormc.hmapro.manager.ui.component.PullToRefresh
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar

@Composable
fun SharedTransitionScope.PresetOverviewScreen() {
    val context = LocalContext.current
    val moduleStatus = LocalModuleStatus.current
    val scope = rememberCoroutineScope()
    var isInitial by rememberSaveable { mutableStateOf(true) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val presets = remember { mutableStateMapOf<HMAConfig.AppPreset, List<InstalledPackage>>() }
    suspend fun refreshPresets() {
        val source = withContext(Dispatchers.IO) {
            ServiceClient.getInstalledPackages().orEmpty()
        }
        HMAConfig.AppPreset.entries.associateWith {
            it.resolveApps(source).mapNotNull { app ->
                app.toInstallPackage(context.packageManager)
            }
        }.apply {
            presets.clear()
            presets.putAll(this)
        }
    }

    LaunchedEffect(Unit) {
        refreshPresets()
        isInitial = false
    }

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
        if (moduleStatus == ModuleStatus.ServiceNotRunning) {
            EmptyContent(
                text = stringResourceBy("home_xposed_service_off")
            )
            return@Scaffold
        }

        if (isInitial) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                InfiniteProgressIndicator()
            }
            return@Scaffold
        }

        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    refreshPresets()
                    isRefreshing = false
                }
            },
            contentPadding = paddingValues
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollEffect(enableOverScroll = false),
                contentPadding = paddingValues.withSafeHorizontal()
            ) {
                items(items = presets.entries.toList()) {
                    PresetItem(it.key, it.value)
                }
            }
        }
    }
}

@Composable
private fun PresetItem(preset: HMAConfig.AppPreset, apps: List<InstalledPackage>) {
    if (apps.isEmpty()) return

    SmallTitle(
        text = stringResourceFromModule(preset.message),
        insideMargin = PaddingValues(16.dp, 8.dp)
    )

    Card {
        Column {
            for ((label, icon) in apps) {
                BasicComponent(
                    title = label,
                    startAction = {
                        Row {
                            AppIcon(icon = icon)

                            Spacer(modifier = Modifier.width(5.dp))
                        }
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}