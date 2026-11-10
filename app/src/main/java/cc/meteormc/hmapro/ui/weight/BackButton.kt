package cc.meteormc.hmapro.ui.weight

import androidx.compose.runtime.Composable
import cc.meteormc.hmapro.ui.util.LocalNavigator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun BackButton() {
    val navigator = LocalNavigator.current
    IconButton(
        onClick = { navigator.pop() }
    ) {
        Icon(
            imageVector = MiuixIcons.Back,
            contentDescription = "Back"
        )
    }
}