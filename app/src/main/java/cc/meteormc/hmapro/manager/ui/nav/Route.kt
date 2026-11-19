package cc.meteormc.hmapro.manager.ui.nav

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
    data object ScopeList : Route

    @Serializable
    data class ScopeSettings(val title: String, val packageName: String) : Route

    @Serializable
    data object TemplateList : Route

    @Serializable
    data class TemplateSettings(val template: String) : Route

    @Serializable
    data object PresetOverview : Route
}