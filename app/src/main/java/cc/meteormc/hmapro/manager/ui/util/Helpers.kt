package cc.meteormc.hmapro.manager.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

fun Modifier.then(block: Modifier.() -> Modifier): Modifier {
    return this.then(Modifier.block())
}

fun Modifier.verticalScrollEffect(scrollBehavior: ScrollBehavior? = null): Modifier {
    return this
        .scrollEndHaptic()
        .overScrollVertical()
        .then {
            if (scrollBehavior == null) this
            else nestedScroll(scrollBehavior.nestedScrollConnection)
        }
}

@Composable
fun PaddingValues.copy(
    start: Dp = Dp.Unspecified,
    top: Dp = Dp.Unspecified,
    end: Dp = Dp.Unspecified,
    bottom: Dp = Dp.Unspecified
): PaddingValues {
    val ld = LocalLayoutDirection.current
    return PaddingValues(
        start = if (start != Dp.Unspecified) start
                else calculateStartPadding(ld),
        top = if (top != Dp.Unspecified) top
              else calculateTopPadding(),
        end = if (end != Dp.Unspecified) end
              else calculateEndPadding(ld),
        bottom = if (bottom != Dp.Unspecified) bottom
                 else calculateBottomPadding()
    )
}

@Composable
fun rememberScreenHeightRatio(): Float {
    return LocalWindowInfo.current.containerSize.height / 800f
}