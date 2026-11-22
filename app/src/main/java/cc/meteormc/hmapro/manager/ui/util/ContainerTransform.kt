package cc.meteormc.hmapro.manager.ui.util

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@Composable
context(scope: SharedTransitionScope)
fun Modifier.containerTransform(
    containerKey: String,
    enter: EnterTransition = fadeIn(tween(150)),
    exit: ExitTransition = fadeOut(tween(150, delayMillis = 300)),
    boundsTransform: BoundsTransform = { _, _ ->
        spring(
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = Rect.VisibilityThreshold
        )
    },
    resizeMode: ResizeMode = ResizeMode.scaleToBounds(ContentScale.Crop)
): Modifier {
    return with(scope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(containerKey),
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
            enter = enter,
            exit = exit,
            boundsTransform = boundsTransform,
            resizeMode = resizeMode
        )
    }
}