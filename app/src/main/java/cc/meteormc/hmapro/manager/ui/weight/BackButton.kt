package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.dropUnlessStarted
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun BackButton(
    icon: ImageVector = MiuixIcons.Back,
    onBack: (() -> Unit)? = null
) {
    val navigator = LocalNavigator.current
    IconButton(
        onClick = dropUnlessStarted {
            onBack?.invoke()
            navigator.back()
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Back"
        )
    }
}