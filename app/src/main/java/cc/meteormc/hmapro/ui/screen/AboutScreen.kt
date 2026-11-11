package cc.meteormc.hmapro.ui.screen

import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.ui.nav.Route
import cc.meteormc.hmapro.ui.util.LocalHMAVersion
import cc.meteormc.hmapro.ui.util.LocalNavigator
import cc.meteormc.hmapro.ui.util.copy
import cc.meteormc.hmapro.ui.util.findResource
import cc.meteormc.hmapro.ui.util.rememberScreenHeightRatio
import cc.meteormc.hmapro.ui.util.stringResourceBy
import cc.meteormc.hmapro.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.ui.weight.BackButton
import cc.meteormc.hmapro.ui.weight.aurora.AuroraBackground
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen() {
    val backdrop = rememberLayerBackdrop()
    val scrollBehavior = MiuixScrollBehavior()
    var scrollProgress by remember { mutableFloatStateOf(0f) }
    val useRuntimeShader = remember { isRuntimeShaderSupported() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_about"),
                color = MiuixTheme.colorScheme.surface.copy(
                    alpha = if (scrollProgress != 1f) 0f else 1f
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->
        AboutContent(
            padding = paddingValues,
            backdrop = backdrop,
            scrollBehavior = scrollBehavior,
            onProgressChange = { scrollProgress = it },
            useRuntimeShader = useRuntimeShader
        )
    }
}

@Composable
private fun AboutContent(
    padding: PaddingValues,
    backdrop: LayerBackdrop,
    scrollBehavior: ScrollBehavior,
    onProgressChange: (Float) -> Unit,
    useRuntimeShader: Boolean
) {
    val listState = rememberLazyListState()
    var headerHeight by remember { mutableIntStateOf(0) }
    val scrollProgress by remember {
        derivedStateOf {
            if (headerHeight <= 0) {
                0f
            } else {
                val index = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (index > 0) 1f
                else (offset.toFloat() / headerHeight).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(scrollProgress) {
        onProgressChange(scrollProgress)
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val version = LocalHMAVersion.current
    val ratio = rememberScreenHeightRatio()
    val safePadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val icon = remember {
        context.findResource("ic_launcher", "mipmap") { resources, id ->
            resources.getDrawable(id, null)
        }
    }

    AuroraBackground(
        // TODO: 浅色模式
        isDarkTheme = true,
        dynamicBackground = true,
        modifier = Modifier.fillMaxSize(),
        bgModifier = Modifier.layerBackdrop(backdrop),
        isFullSize = true,
        effectBackground = useRuntimeShader,
        alpha = { 1f - scrollProgress }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(1f - scrollProgress)
                .onSizeChanged { size -> headerHeight = size.height }
                .padding(
                    safePadding.copy(
                        top = padding.calculateTopPadding() + (24 * ratio).dp,
                        bottom = 0.dp
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                modifier = Modifier.size(128.dp),
                factory = { context ->
                    ImageView(context).apply {
                        setImageDrawable(icon)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                }
            )

            Column(
                modifier = Modifier
                    .padding(top = (32 * ratio).dp)
                    .graphicsLayer {
                        translationY = -((36 * ratio).dp.toPx() * scrollProgress)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .textureBlur(
                            backdrop = backdrop,
                            shape = RectangleShape,
                            colors = BlurColors(
                                blendColors = listOf(
                                    BlendColorEntry(
                                        Color.White.copy(alpha = 0.25f),
                                        BlurBlendMode.LinearLight
                                    ),
                                    BlendColorEntry(
                                        MiuixTheme.colorScheme.onSecondary,
                                        BlurBlendMode.ColorDodge
                                    )
                                )
                            ),
                            contentBlendMode = BlendMode.DstIn,
                            enabled = useRuntimeShader
                        ),
                    text = stringResourceBy("app_name"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.8f),
                    text = "v${version.fullName}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(scrollBehavior),
            contentPadding = safePadding.copy(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item("spacer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { headerHeight.toDp() })
                ) {

                }
            }

            item("body") {
                AboutBody(
                    backdrop = backdrop,
                    useRuntimeShader = useRuntimeShader
                )
            }
        }
    }
}

@Composable
private fun AboutBody(
    backdrop: Backdrop,
    useRuntimeShader: Boolean
) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AboutCard(
            backdrop = backdrop,
            title = stringResourceFromModule(R.string.title_description),
            insideMargin = BasicComponentDefaults.InsideMargin,
            useRuntimeShader = useRuntimeShader
        ) {
            Text(
                text = stringResourceBy("about_description"),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AboutCard(
            backdrop = backdrop,
            title = stringResourceBy("about_how_to_use_title"),
            insideMargin = BasicComponentDefaults.InsideMargin,
            useRuntimeShader = useRuntimeShader
        ) {
            val line1 = stringResourceBy("about_how_to_use_description_1")
            val line2 = stringResourceBy("about_how_to_use_description_2")
            Text(
                text = "$line1\n\n$line2",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AboutCard(
            backdrop = backdrop,
            title = stringResourceBy("title_about"),
            useRuntimeShader = useRuntimeShader
        ) {
            ArrowPreference(
                title = stringResourceFromModule(R.string.view_source_code),
                onClick = { uriHandler.openUri("https://github.com/Dr-TSNG/Hide-My-Applist") }
            )
            ArrowPreference(
                title = stringResourceFromModule(R.string.join_telegram),
                onClick = { uriHandler.openUri("https://t.me/HideMyApplist") }
            )
            ArrowPreference(
                title = stringResourceBy("about_open_source"),
                onClick = { navigator.push(Route.OpenSourceLicenses) }
            )
            ArrowPreference(
                title = stringResourceBy("about_developer"),
                onClick = { navigator.push(Route.Developers) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = buildAnnotatedString {
                append("Powered by ")

                val start = length
                append(stringResourceFromModule(R.string.app_name))
                addLink(
                    url = LinkAnnotation.Url(
                        url = "https://github.com/Meteor2333/HMA-Pro",
                        styles = TextLinkStyles(
                            style = SpanStyle(textDecoration = TextDecoration.Underline)
                        )
                    ),
                    start = start,
                    end = length
                )
            },
            modifier = Modifier.fillMaxWidth(),
            color = MiuixTheme.colorScheme.onSurfaceSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AboutCard(
    backdrop: Backdrop,
    title: String,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    useRuntimeShader: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    SmallTitle(title)
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .textureBlur(
                backdrop = backdrop,
                shape = RoundedCornerShape(CardDefaults.CornerRadius),
                blurRadius = BlurDefaults.MaxBlurRadius,
                colors = BlurColors(
                    blendColors = listOf(
                        BlendColorEntry(Color(0x4DA9A9A9), BlurBlendMode.Luminosity),
                        BlendColorEntry(Color(0x1A9C9C9C), BlurBlendMode.PlusDarker)
                    )

                    // TODO: 适配浅色模式
                    // listOf(
                    //     BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay),
                    //     BlendColorEntry(Color(0xB3FFFFFF), BlurBlendMode.HardLight)
                    // )
                ),
                enabled = useRuntimeShader
            ),
        insideMargin = insideMargin,
        colors = CardDefaults.defaultColors(
            color = Color.Transparent
        ),
        content = content
    )
}