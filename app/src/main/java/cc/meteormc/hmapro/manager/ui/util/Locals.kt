package cc.meteormc.hmapro.manager.ui.util

import android.content.res.Resources
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import cc.meteormc.hmapro.manager.data.HMAVersion
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.ui.nav.Navigator
import cc.meteormc.hmapro.manager.ui.preference.PreferencesState

val LocalHMAVersion = staticCompositionLocalOf {
    HMAVersion(
        fullName = "Unknown",
        simpleName = "Unknown",
        hash = "unknown",
        code = -1L
    )
}

val LocalModuleResources = compositionLocalOf<Resources> {
    error("ModuleResources not provided")
}

val LocalModuleStatus = compositionLocalOf<ModuleStatus> {
    ModuleStatus.Disabled
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator not provided")
}

val LocalPreferencesState = staticCompositionLocalOf<PreferencesState> {
    error("PreferenceState not provided")
}