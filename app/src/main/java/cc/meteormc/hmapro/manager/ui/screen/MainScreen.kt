package cc.meteormc.hmapro.manager.ui.screen

import android.app.DownloadManager
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.data.UpdateInfo
import cc.meteormc.hmapro.manager.ui.page.HomePage
import cc.meteormc.hmapro.manager.ui.page.LogsPage
import cc.meteormc.hmapro.manager.ui.page.SettingsPage
import cc.meteormc.hmapro.manager.ui.util.DeviceType
import cc.meteormc.hmapro.manager.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.manager.ui.util.currentDeviceType
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MainScreen() {
    val deviceType = currentDeviceType()
    val backdrop = rememberLayerBackdrop()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val homeTitle = stringResourceBy("title_home")
    val logsTitle = stringResourceBy("title_logs")
    val settingsTitle = stringResourceBy("title_settings")
    val pages = remember(homeTitle, logsTitle, settingsTitle) {
        listOf(
            NavigationPage(
                label = homeTitle,
                icon = MiuixIcons.Home,
                content = { HomePage() }
            ),
            NavigationPage(
                label = logsTitle,
                icon = MiuixIcons.Notes,
                content = { LogsPage() }
            ),
            NavigationPage(
                label = settingsTitle,
                icon = MiuixIcons.Settings,
                content = { SettingsPage() }
            )
        )
    }

    val slideSpec = tween<IntOffset>(durationMillis = 350)
    val fadeSpec = tween<Float>(durationMillis = 300, easing = LinearOutSlowInEasing)

    if (deviceType == DeviceType.PAD) {
        Row {
            NavigationRail(
                modifier = Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape
                ),
                expanded = true,
                expandedWidth = 160.dp
            ) {
                Spacer(modifier = Modifier.weight(1f))

                pages.forEachIndexed { index, page ->
                    NavigationRailItem(
                        label = page.label,
                        icon = page.icon,
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )

                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                val topOffset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(modifier = Modifier.height(topOffset))
            }

            val insets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
            MainScreenContent(
                paddingValues = insets.only(WindowInsetsSides.End).asPaddingValues(),
                backdrop = backdrop,
                selectedIndex = selectedIndex,
                pages = pages,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically(
                            animationSpec = slideSpec,
                            initialOffsetY = { height -> height }
                        ) + fadeIn(fadeSpec) togetherWith slideOutVertically(
                            animationSpec = slideSpec,
                            targetOffsetY = { height -> -height }
                        ) + fadeOut(fadeSpec)
                    } else {
                        slideInVertically(
                            animationSpec = slideSpec,
                            initialOffsetY = { height -> -height }
                        ) + fadeIn(fadeSpec) togetherWith slideOutVertically(
                            animationSpec = slideSpec,
                            targetOffsetY = { height -> height }
                        ) + fadeOut(fadeSpec)
                    }
                }
            )
        }
    } else {
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
            }
        ) { paddingValues ->
            MainScreenContent(
                paddingValues = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                backdrop = backdrop,
                selectedIndex = selectedIndex,
                pages = pages,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { width -> width }
                        ) + fadeIn(fadeSpec) togetherWith slideOutHorizontally(
                            animationSpec = slideSpec,
                            targetOffsetX = { width -> -width }
                        ) + fadeOut(fadeSpec)
                    } else {
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { width -> -width }
                        ) + fadeIn(fadeSpec) togetherWith slideOutHorizontally(
                            animationSpec = slideSpec,
                            targetOffsetX = { width -> width }
                        ) + fadeOut(fadeSpec)
                    }
                }
            )
        }
    }

    UpdateDialog()
}

@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    backdrop: LayerBackdrop,
    selectedIndex: Int,
    pages: List<NavigationPage>,
    transitionSpec: AnimatedContentTransitionScope<Int>.() -> ContentTransform
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .layerBackdrop(backdrop),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = selectedIndex,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = transitionSpec
        ) { index ->
            pages[index].content()
        }
    }
}

@Composable
private fun UpdateDialog() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentVersion = LocalHMAVersion.current

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf(UpdateInfo("", "", "", null)) }
    LaunchedEffect(Unit) {
        val result = UpdateInfo.checkUpdate(currentVersion) ?: return@LaunchedEffect
        updateInfo = result
        showUpdateDialog = true
    }

    WindowDialog(
        show = showUpdateDialog,
        modifier = Modifier.padding(horizontal = 16.dp),
        title = stringResourceBy("home_new_update", updateInfo.version),
        largeScreen = true
    ) {
        Text(
            text = updateInfo.changelog,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 196.dp)
                .verticalScroll(rememberScrollState()),
            color = DialogDefaults.summaryColor(),
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        val asset = updateInfo.asset
        if (asset != null) {
            Text(
                text = buildAnnotatedString {
                    val start = length
                    append(stringResourceFromModule(R.string.open_release_page))
                    addLink(
                        url = LinkAnnotation.Url(
                            url = updateInfo.releaseUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(textDecoration = TextDecoration.Underline)
                            )
                        ),
                        start = start,
                        end = length
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            TextButton(
                text = stringResource(android.R.string.cancel),
                onClick = { showUpdateDialog = false },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(18.dp))

            if (asset != null) {
                TextButton(
                    text = stringResourceFromModule(R.string.download),
                    onClick = {
                        showUpdateDialog = false
                        val request = DownloadManager.Request(asset.downloadUrl.toUri())
                        request.setTitle(asset.name)
                        request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        )
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            asset.name
                        )
                        val manager = context.getSystemService<DownloadManager>()
                        val taskId = manager?.enqueue(request) ?: -1
                        if (taskId < 0L) {
                            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            } else {
                TextButton(
                    text = stringResourceFromModule(R.string.open_release_page),
                    onClick = { uriHandler.openUri(updateInfo.releaseUrl) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Immutable
private data class NavigationPage(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)