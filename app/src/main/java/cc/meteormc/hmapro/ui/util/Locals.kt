package cc.meteormc.hmapro.ui.util

import android.content.res.Resources
import androidx.compose.runtime.staticCompositionLocalOf
import cc.meteormc.hmapro.data.HMAVersion
import cc.meteormc.hmapro.data.ModuleStatus
import top.yukonga.miuix.kmp.nav.core.NavController

val LocalContainerTransformState = staticCompositionLocalOf<ContainerTransformState> {
    error("ContainerTransformState not provided")
}

val LocalHMAVersion = staticCompositionLocalOf {
    HMAVersion(
        fullName = "Unknown",
        simpleName = "Unknown",
        hash = "unknown",
        code = -1L
    )
}

val LocalModuleResources = staticCompositionLocalOf<Resources> {
    error("ModuleResources not provided")
}

val LocalModuleStatus = staticCompositionLocalOf<ModuleStatus> {
    ModuleStatus.Disabled
}

val LocalNavigator = staticCompositionLocalOf<NavController> {
    error("NavController not provided")
}