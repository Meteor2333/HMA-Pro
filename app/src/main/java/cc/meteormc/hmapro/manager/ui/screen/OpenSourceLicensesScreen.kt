package cc.meteormc.hmapro.manager.ui.screen

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.manager.ui.util.copy
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class Library(
    val name: String,
    val author: String,
    val license: String,
    val url: String
)

private const val LICENSE_APACHE_2 = "Apache Software License 2.0"
private const val LICENSE_MIT = "MIT License"

private val libraries = listOf(
    Library(
        "MultiType",
        "drakeet",
        LICENSE_APACHE_2,
        "https://github.com/drakeet/MultiType"
    ),
    Library(
        "about-page",
        "drakeet",
        LICENSE_APACHE_2,
        "https://github.com/drakeet/about-page"
    ),
    Library(
        "libxposed",
        "libxposed",
        LICENSE_APACHE_2,
        "https://github.com/libxposed"
    ),
    Library(
        "EzHookTool",
        "lingqiqi5211",
        LICENSE_MIT,
        "https://github.com/lingqiqi5211/EzHookTool"
    ),
    Library(
        "libsu",
        "topjohnwu",
        LICENSE_APACHE_2,
        "https://github.com/topjohnwu/libsu"
    ),
    Library(
        "okhttp",
        "square",
        LICENSE_APACHE_2,
        "https://github.com/square/okhttp"
    ),
    Library(
        "rxhttp",
        "liujingxing",
        LICENSE_APACHE_2,
        "https://github.com/liujingxing/rxhttp"
    ),
    Library(
        "miuix",
        "yukonga",
        LICENSE_APACHE_2,
        "https://github.com/compose-miuix-ui/miuix"
    ),
    Library(
        "DexKit",
        "LuckyPray",
        LICENSE_APACHE_2,
        "https://github.com/LuckyPray/DexKit"
    )
)

@Composable
fun OpenSourceLicensesScreen() {
    val scrollBehavior = MiuixScrollBehavior()
    val horizontalPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResourceBy("about_open_source"),
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .verticalScrollEffect(scrollBehavior),
            contentPadding = horizontalPadding.copy(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            items(
                items = libraries,
                key = { it.name }
            ) {
                LibraryCard(it)
            }
        }
    }
}

@Composable
private fun LibraryCard(library: Library) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        onClick = { uriHandler.openUri(library.url) },
        showIndication = true
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = library.name,
                modifier = Modifier.basicMarquee(),
                fontSize = 18.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = library.author,
                color = MiuixTheme.colorScheme.onSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = library.license,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    color = MiuixTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}