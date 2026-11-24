package cc.meteormc.hmapro.manager.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.ui.util.PaddingValuesSides
import cc.meteormc.hmapro.manager.ui.util.only
import cc.meteormc.hmapro.manager.ui.util.useBlur
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.basic.TopAppBarLayout
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PageTopAppBar(
    title: String,
    padding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {}
) {
    val horizontalPadding = padding.only(PaddingValuesSides.Horizontal)
    if (useBlur()) {
        TopAppBarLayout(
            title = title,
            color = MiuixTheme.colorScheme.surface,
            titleColor = MiuixTheme.colorScheme.onSurface,
            largeTitleColor = MiuixTheme.colorScheme.onSurface,
            subtitle = "",
            subtitleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            navigationIcon = {},
            actions = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            },
            titlePadding = TopAppBarDefaults.TitlePadding,
            navigationIconPadding = TopAppBarDefaults.NavigationIconPadding,
            actionIconPadding = TopAppBarDefaults.ActionIconPadding,
            scrollBehavior = scrollBehavior,
            modifier = Modifier
                .padding(horizontalPadding)
                .then(modifier),
            titleBlurRadius = BlurDefaults.BlurRadius.dp,
            defaultWindowInsetsPadding = false,
            bottomContent = bottomContent,
        )
    } else {
        TopAppBar(
            title = title,
            modifier = Modifier
                .padding(horizontalPadding)
                .then(modifier),
            scrollBehavior = scrollBehavior,
            actions = actions,
            defaultWindowInsetsPadding = false,
            bottomContent = bottomContent
        )
    }
}