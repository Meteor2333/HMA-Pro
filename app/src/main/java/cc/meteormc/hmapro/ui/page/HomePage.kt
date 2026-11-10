package cc.meteormc.hmapro.ui.page

import androidx.compose.animation.core.animate
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.data.ModuleStatus
import cc.meteormc.hmapro.ui.nav.Route
import cc.meteormc.hmapro.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.ui.util.LocalNavigator
import cc.meteormc.hmapro.ui.util.stringResourceBy
import cc.meteormc.hmapro.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.ui.util.verticalScrollEffect
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Unpin
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.tanh

@Composable
fun HomePage() {
    val navigator = LocalNavigator.current
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceFromModule(R.string.app_name),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = { navigator.push(Route.About) }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Info,
                            contentDescription = "About"
                        )
                    }
                }
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
                item("module_status") {
                    ModuleStatusCard()
                }
            }
        }
    )
}

@Composable
private fun ModuleStatusCard() {
    val warningContainer = Color(0xFFFFF3CD)

    val version = LocalHMAVersion.current
    var title = stringResourceBy(
        "home_xposed_activated",
        version.simpleName,
        version.code
    )
    var summary = ""
    var icon: ImageVector
    val color: Color
    when (val status = LocalModuleStatus.current) {
        ModuleStatus.Disabled -> {
            title = stringResourceBy("home_xposed_not_activated")
            icon = MiuixIcons.Medium.Close
            color = MiuixTheme.colorScheme.errorContainer
        }
        ModuleStatus.OutOfDate -> {
            summary = stringResourceBy("home_xposed_service_old")
            icon = MiuixIcons.Medium.Update
            color = warningContainer
        }
        ModuleStatus.ServiceNotActivated -> {
            summary = stringResourceBy("home_xposed_service_off") + stringResourceFromModule(R.string.reboot_tip)
            icon = MiuixIcons.Medium.Refresh
            color = warningContainer
        }
        ModuleStatus.NativeHookUnavailable -> {
            title = stringResourceBy("home_native_hook_disabled_title")
            summary = stringResourceBy("home_native_hook_disabled_summary")
            icon = MiuixIcons.Medium.Unpin
            color = warningContainer
        }
        is ModuleStatus.Running -> {
            summary = stringResourceBy("home_xposed_service_on", status.serviceVersion)
            icon = MiuixIcons.Medium.Ok
            color = MiuixTheme.colorScheme.secondaryContainer
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
                cameraDistance = 6f * density
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
                                block = { value, _ -> dx = value }
                            )
                        }

                        scope.launch {
                            animate(
                                initialValue = dy,
                                targetValue = 0f,
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
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.6f),
                            color
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 28.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = "Status Icon",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(24.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = summary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}