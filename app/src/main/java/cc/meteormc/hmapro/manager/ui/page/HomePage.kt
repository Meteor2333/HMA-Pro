package cc.meteormc.hmapro.manager.ui.page

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.BuildConfig
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.copy
import cc.meteormc.hmapro.manager.ui.util.isDarkMode
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import cc.meteormc.hmapro.manager.ui.weight.PageTopAppBar
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Help
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.tanh

@Composable
fun SharedTransitionScope.HomePage(outerPadding: PaddingValues) {
    val navigator = LocalNavigator.current
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PageTopAppBar(
                title = stringResourceBy("app_name"),
                padding = outerPadding,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = navigator.navigate(Route.About)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Help,
                            contentDescription = "About"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(scrollBehavior = scrollBehavior),
            contentPadding = outerPadding
                .withSafeHorizontal()
                .copy(top = innerPadding.calculateTopPadding())
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { ModuleStatusCard() }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { InfoCard() }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { ManageCard() }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ModuleStatusCard() {
    val warningContainer = if (isDarkMode()) Color(0xCB553C25) else Color(0xCBFFD0A6)
    val errorContainer = if (isDarkMode()) Color(0xCD4E1616) else Color(0xCBFFCFCF)

    val title: String
    val summary: String
    val icon: ImageVector
    val background: Brush
    when (LocalModuleStatus.current) {
        ModuleStatus.Disabled -> {
            title = stringResourceBy("home_xposed_not_activated")
            summary = stringResourceBy("home_xposed_service_disabled")
            icon = MiuixIcons.Medium.Close
            background = SolidColor(errorContainer)
        }
        ModuleStatus.OutOfDate -> {
            title = stringResourceFromModule(R.string.module_activated)
            summary = stringResourceBy("home_xposed_service_old")
            icon = MiuixIcons.Medium.Update
            background = SolidColor(warningContainer)
        }
        ModuleStatus.ServiceNotRunning -> {
            title = stringResourceFromModule(R.string.module_activated)
            summary = stringResourceBy("home_xposed_service_off") + stringResourceFromModule(R.string.reboot_tip)
            icon = MiuixIcons.Medium.Refresh
            background = SolidColor(warningContainer)
        }
        is ModuleStatus.Running -> {
            title = stringResourceFromModule(R.string.module_activated)
            summary = stringResourceFromModule(R.string.service_running)
            icon = MiuixIcons.Medium.Ok

            val containerColor = if (MiuixTheme.isDynamicColor) {
                MiuixTheme.colorScheme.secondaryContainer
            } else {
                MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            }
            background = Brush.linearGradient(
                colors = listOf(containerColor, containerColor.copy(alpha = containerColor.alpha - 0.2f)),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }
    }

    val scope = rememberCoroutineScope()
    var dx by remember { mutableFloatStateOf(0f) }
    var dy by remember { mutableFloatStateOf(0f) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = dx * 24f
                translationY = dy * 12f
                rotationY = dx * 12f
                rotationX = -dy * 6f
                cameraDistance = 4f * density
            }
            .pointerInput(Unit) {
                fun curve(v: Float): Float {
                    val sign = sign(v)
                    val abs = abs(v)
                    return sign * tanh(abs * 0.32f)
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    val anchor = down.position

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.singleOrNull()
                            if (change == null || change.id != pointerId || !change.pressed) {
                                break
                            }

                            if (change.position != change.previousPosition) {
                                val delta = change.position - anchor
                                dx = curve(delta.x / (size.width / 2f))
                                dy = curve(delta.y / (size.height / 2f))
                                change.consume()
                            }
                        }
                    } finally {
                        scope.launch {
                            animate(
                                initialValue = dx,
                                targetValue = 0f,
                                animationSpec = spring(),
                                block = { value, _ -> dx = value }
                            )
                        }

                        scope.launch {
                            animate(
                                initialValue = dy,
                                targetValue = 0f,
                                animationSpec = spring(),
                                block = { value, _ -> dy = value }
                            )
                        }
                    }
                }
            },
        colors = CardDefaults.defaultColors(
            color = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = background)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = "Status Icon",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = summary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (!ServiceClient.isNativeHookSupported()) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.tertiaryContainer,
                contentColor = MiuixTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResourceBy("home_native_hook_disabled_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResourceBy("home_native_hook_disabled_summary"),
                    fontSize = 16.sp
                )
            }
        }

    }
}

@Composable
private fun InfoCard() {
    val version = LocalHMAVersion.current
    val status = LocalModuleStatus.current
    Card(
        insideMargin = PaddingValues(
            horizontal = 18.dp,
            vertical = 10.dp
        )
    ) {
        BasicComponent(
            title = stringResourceFromModule(R.string.module_version),
            summary = version.simpleName + '-' + version.code,
            insideMargin = PaddingValues(vertical = 8.dp)
        )
        BasicComponent(
            title = stringResourceFromModule(R.string.service_version),
            summary = if (status is ModuleStatus.Running) status.serviceVersion.toString() else "Unknown",
            insideMargin = PaddingValues(vertical = 8.dp)
        )
        BasicComponent(
            title = stringResourceFromModule(R.string.hmapro_version),
            summary = BuildConfig.VERSION_NAME,
            insideMargin = PaddingValues(vertical = 8.dp)
        )
        BasicComponent(
            title = stringResourceFromModule(R.string.force_mount),
            summary = stringResourceBy(if (ConfigManager.forceMountData) "enabled" else "disabled"),
            insideMargin = PaddingValues(vertical = 8.dp)
        )
    }
}

@Composable
private fun SharedTransitionScope.ManageCard() {
    val navigator = LocalNavigator.current
    val colors = CardDefaults.defaultColors()
    val cornerRadius = CardDefaults.CornerRadius
    Card(
        cornerRadius = cornerRadius,
        colors = colors
    ) {
        ArrowPreference(
            title = stringResourceBy("title_app_manage"),
            modifier = Modifier.containerTransform("scope_manage"),
            onClick = navigator.navigate(Route.ScopeList),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.GridView,
                    contentDescription = "Scope Manage"
                )
            }
        )
        ArrowPreference(
            title = stringResourceBy("title_template_manage"),
            modifier = Modifier.containerTransform("template_manage"),
            onClick = navigator.navigate(Route.TemplateList),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Backup,
                    contentDescription = "Template Manage"
                )
            }
        )
        ArrowPreference(
            title = stringResourceFromModule(R.string.preset_overview),
            modifier = Modifier.containerTransform("preset_overview"),
            onClick = navigator.navigate(Route.PresetOverview),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Background,
                    contentDescription = "Preset Manage"
                )
            }
        )
    }
}