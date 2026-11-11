package cc.meteormc.hmapro.ui.nav

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Developers : Route

    @Serializable
    data object OpenSourceLicenses : Route

    @Serializable
    data object AppManage : Route

    @Serializable
    data object TemplateManage : Route

    @Serializable
    data object PresetOverview : Route
}