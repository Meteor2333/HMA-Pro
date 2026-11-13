package cc.meteormc.hmapro.manager.ui.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.navDirectionalTransition
import kotlin.math.pow

fun containerTransform(state: ContainerTransformState) = navDirectionalTransition(
    push = containerTransformPush(state),
    pop = containerTransformPop(state)
)

private fun containerTransformPush(state: ContainerTransformState) = NavTransition { scope ->
    if (state.dirty) {
        state.reset()
    }

    if (scope.role != NavRole.Incoming) {
        return@NavTransition graphicsLayer {
            clip = false
        }
    }

    val progress = scope.calculateProgress()
    graphicsLayer {
        clip = true
        shape = ExpandingRectShape(progress, state.rect.value, state.cornerRadius.value)
    }
}

private fun containerTransformPop(state: ContainerTransformState) = NavTransition { scope ->
    state.dirty = true

    if (scope.role != NavRole.Outgoing) {
        return@NavTransition graphicsLayer {
            clip = false
            alpha = 1f
        }
    }

    val progress = scope.calculateProgress()
    graphicsLayer {
        clip = true
        shape = ExpandingRectShape(progress, state.rect.value, state.cornerRadius.value)
        alpha = (progress / 0.05f).coerceIn(0f, 1f).pow(0.5f)
    }.drawWithContent {
        drawContent()

        val color = state.color.value ?: return@drawWithContent
        drawRect(
            lerp(Color.Transparent, color, 1 - progress)
        )
    }
}

private fun NavTransitionScope.calculateProgress(): Float {
    return (relativeDepth + 1f).coerceIn(0f, 1f)
}

class ContainerTransformState {
    internal var dirty = true
    var rect = mutableStateOf<Rect?>(null)
        set(value) {
            dirty = false
            field = value
        }
    var color = mutableStateOf<Color?>(null)
        set(value) {
            dirty = false
            field = value
        }
    var cornerRadius = mutableStateOf<Dp?>(null)
        set(value) {
            dirty = false
            field = value
        }

    fun reset() {
        rect = mutableStateOf(null)
        color = mutableStateOf(null)
        cornerRadius = mutableStateOf(null)
    }
}

private class ExpandingRectShape(
    private val progress: Float,
    private val rect: Rect?,
    private val cornerRadius: Dp?
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        var left = (rect?.left ?: centerX).coerceIn(0f, size.width)
        var top = (rect?.top ?: centerY).coerceIn(0f, size.height)
        var right = (rect?.right ?: centerX).coerceIn(0f, size.width)
        var bottom = (rect?.bottom ?: centerY).coerceIn(0f, size.height)
        var radius = with(density) { cornerRadius?.toPx() } ?: 0f

        val full = Rect(Offset.Zero, size)
        left = lerp(left, full.left, progress)
        top = lerp(top, full.top, progress)
        right = lerp(right, full.right, progress)
        bottom = lerp(bottom, full.bottom, progress)
        radius *= ((1f - progress) / 0.65f).coerceIn(0f, 1f)
        return Outline.Rounded(
            RoundRect(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                radiusX = radius,
                radiusY = radius
            )
        )
    }
}
