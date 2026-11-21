package cc.meteormc.hmapro.manager.ui.util

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@Composable
context(scope: SharedTransitionScope)
fun Modifier.containerTransform(
    containerKey: String,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    resizeMode: ResizeMode = ResizeMode.RemeasureToBounds
): Modifier {
    return with(scope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(containerKey),
            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
            enter = enter,
            exit = exit,
            resizeMode = resizeMode
        )
    }
}