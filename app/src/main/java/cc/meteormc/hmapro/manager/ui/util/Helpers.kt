package cc.meteormc.hmapro.manager.ui.util

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.ManagerMain
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

fun Activity.restart() {
    val intent = Intent().apply {
        setClassName(
            this@restart,
            ManagerMain.MAIN_ACTIVITY
        )
    }
    finishAffinity()
    startActivity(intent)
}

fun Resources.updateConfiguration(block: Configuration.() -> Unit) {
    val config = configuration
    config.block()
    @Suppress("DEPRECATION")
    updateConfiguration(config, displayMetrics)
}

fun Modifier.then(block: Modifier.() -> Modifier): Modifier {
    return this.then(Modifier.block())
}

fun Modifier.verticalScrollEffect(
    enableHaptic: Boolean = true,
    enableOverScroll: Boolean = true,
    scrollBehavior: ScrollBehavior? = null
): Modifier {
    return this
        .then {
            if (!enableHaptic) this
            else scrollEndHaptic()
        }
        .then {
            if (!enableOverScroll) this
            else overScrollVertical()
        }
        .then {
            if (scrollBehavior == null) this
            else nestedScroll(scrollBehavior.nestedScrollConnection)
        }
}

fun <T> Collection<T>.toMutableStateSet() = mutableStateSetOf<T>().also { it.addAll(this) }

fun <K, V> Map<K, V>.toMutableStateMap() = mutableStateMapOf<K, V>().also { it.putAll(this) }

enum class PaddingValuesSides {
    Start, End, Top, Bottom, Horizontal, Vertical
}

@Composable
fun PaddingValues.only(sides: PaddingValuesSides): PaddingValues {
    val direction = LocalLayoutDirection.current
    val isHorizontal = sides == PaddingValuesSides.Horizontal
    val isVertical = sides == PaddingValuesSides.Vertical
    return PaddingValues(
        start = if (sides == PaddingValuesSides.Start || isHorizontal) calculateStartPadding(direction) else 0.dp,
        top = if (sides == PaddingValuesSides.Top || isVertical) calculateTopPadding() else 0.dp,
        end = if (sides == PaddingValuesSides.End || isHorizontal) calculateEndPadding(direction) else 0.dp,
        bottom = if (sides == PaddingValuesSides.Bottom || isVertical) calculateBottomPadding() else 0.dp
    )
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
fun PaddingValues.withSafeHorizontal(): PaddingValues {
    return plus(PaddingValues(horizontal = 12.dp))
}

@Composable
fun useBlur(): Boolean {
    val prefs = LocalPreferencesState.current
    val supported = remember { isRuntimeShaderSupported() }
    return supported && prefs.enableBlur
}

@Composable
fun isDarkMode(): Boolean {
    return when (MiuixTheme.colorSchemeMode) {
        ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> false
        ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun rememberSnackbarHostState(): SnackbarHostState {
    return remember { SnackbarHostState() }
}