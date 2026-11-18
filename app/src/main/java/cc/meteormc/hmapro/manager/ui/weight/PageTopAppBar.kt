package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.ui.util.PaddingValuesSides
import cc.meteormc.hmapro.manager.ui.util.only
import cc.meteormc.hmapro.manager.ui.util.useBlur
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlurDefaults

@Composable
fun PageTopAppBar(
    title: String,
    padding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {}
) {
    val horizontalPadding = padding.only(PaddingValuesSides.Horizontal)
    if (useBlur()) {
        BlurTopAppBar(
            title = title,
            blurRadius = BlurDefaults.BlurRadius.dp,
            modifier = Modifier.padding(horizontalPadding),
            scrollBehavior = scrollBehavior,
            actions = actions,
            defaultWindowInsetsPadding = false,
            bottomContent = bottomContent
        )
    } else {
        TopAppBar(
            title = title,
            modifier = Modifier.padding(horizontalPadding),
            scrollBehavior = scrollBehavior,
            actions = actions,
            defaultWindowInsetsPadding = false,
            bottomContent = bottomContent
        )
    }
}