package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessStarted
import androidx.navigation3.runtime.result.ResultEffect
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.ui.component.AppIcon
import cc.meteormc.hmapro.manager.ui.component.AppSelectorScaffold
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import cc.meteormc.hmapro.manager.ui.component.dialog.ItemsDialog
import cc.meteormc.hmapro.manager.ui.component.dialog.UnsavedChangesDialog
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.toMutableStateSet
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.EnumSet
import kotlin.reflect.KProperty

@Composable
fun SharedTransitionScope.ScopeListScreen() {
    val navigator = LocalNavigator.current
    AppSelectorScaffold(
        modifier = Modifier.containerTransform("scope_manage"),
        title = stringResourceBy("title_app_manage"),
        enabled = { ScopeConfigState.isScopeEnabled(it.packageName) }
    ) {
        val enabled = ScopeConfigState.isScopeEnabled(it.packageName)
        BasicComponent(
            startAction = {
                Row {
                    AppIcon(
                        icon = it.icon,
                        enabled = enabled
                    )

                    Spacer(modifier = Modifier.width(5.dp))
                }
            },
            onClick = dropUnlessStarted {
                val nav = Route.ScopeSettings(it.label, it.packageName)
                navigator.navigate(nav)
            }
        ) {
            Text(
                text = it.label,
                modifier = Modifier.basicMarquee(),
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurface
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                fontWeight = FontWeight.Medium,
                minLines = 1
            )
            Text(
                text = it.packageName,
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
                fontSize = MiuixTheme.textStyles.body2.fontSize
            )
        }
    }
}

@Composable
fun ScopeSettingsScreen(
    title: String,
    packageName: String
) {
    val navigator = LocalNavigator.current
    val scope = remember(packageName) { ScopeConfigState.value[packageName] }
    var enabled by rememberSaveable(scope) { mutableStateOf(scope != null) }
    var useWhitelist by rememberSaveable(scope) { mutableStateOf(scope?.useWhitelist ?: false) }
    var excludeSystemApps by rememberSaveable(scope) { mutableStateOf(scope?.excludeSystemApps ?: false) }
    var aggressiveFilter by rememberSaveable(scope) { mutableStateOf(scope?.aggressiveFilter ?: false) }
    val applyTemplates = rememberSaveable(scope) { scope?.applyTemplates.orEmpty().toMutableStateSet() }
    val applyPresets = rememberSaveable(scope) { scope?.applyPresets.orEmpty().toMutableStateSet() }
    val extraApps = rememberSaveable(scope) { scope?.extraAppList.orEmpty().toMutableStateSet() }
    val hiddenSettings = rememberSaveable(scope) { scope?.hiddenSettings.orEmpty().toMutableStateSet() }

    var unsaved by rememberSaveable { mutableStateOf(false) }
    fun save() {
        if (!unsaved) return

        val newScopes = ScopeConfigState.value.toMutableMap()
        if (enabled) {
            newScopes[packageName] = HMAConfig.ScopeConfig.new().apply {
                this.aggressiveFilter = aggressiveFilter
                this.useWhitelist = useWhitelist
                this.excludeSystemApps = excludeSystemApps
                this.applyTemplates = applyTemplates
                this.applyPresets = applyPresets
                this.extraAppList = extraApps
                this.hiddenSettings = if (hiddenSettings.isNotEmpty()) {
                    EnumSet.copyOf(hiddenSettings)
                } else {
                    EnumSet.noneOf(HMAConfig.HiddenSetting::class.java)
                }
            }
        } else {
            newScopes.remove(packageName)
        }

        ScopeConfigState.value = newScopes
        unsaved = false
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var showHiddenSettingsDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = unsaved) {
        showUnsavedDialog = true
    }

    ResultEffect<Set<String>>("extra_apps") {
        if (extraApps != it) {
            unsaved = true
            extraApps.clear()
            extraApps.addAll(it)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = { BackButton(MiuixIcons.Close) },
                actions = {
                    IconButton(
                        onClick = dropUnlessStarted {
                            save()
                            navigator.back()
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = "Confirm"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        UnsavedChangesDialog(
            show = showUnsavedDialog,
            onDismiss = { showUnsavedDialog = false },
            onExit = dropUnlessStarted { navigator.back() },
            onSave = { save() }
        )

        ItemsDialog(
            show = showTemplateDialog,
            title = stringResourceBy("app_choose_template"),
            items = ConfigManager.templates
                .filterValues { it.isWhitelist == useWhitelist }
                .keys
                .toList(),
            selected = applyTemplates,
            onCancel = {
                showTemplateDialog = false
                applyTemplates.clear()
                scope?.applyTemplates?.also {
                    applyTemplates.addAll(it)
                }
            },
            onConfirm = {
                unsaved = true
                showTemplateDialog = false
            }
        )

        ItemsDialog(
            show = showPresetDialog,
            title = stringResourceFromModule(R.string.choose_presets),
            items = HMAConfig.AppPreset.entries,
            selected = applyPresets,
            transform = { stringResourceFromModule(it.message) },
            onCancel = {
                showPresetDialog = false
                applyPresets.clear()
                scope?.applyPresets?.also {
                    applyPresets.addAll(it)
                }
            },
            onConfirm = {
                unsaved = true
                showPresetDialog = false
            }
        )

        ItemsDialog(
            show = showHiddenSettingsDialog,
            title = stringResourceFromModule(R.string.choose_settings),
            items = HMAConfig.HiddenSetting.entries,
            selected = hiddenSettings,
            transform = { stringResourceFromModule(it.message) },
            onCancel = {
                showHiddenSettingsDialog = false
                hiddenSettings.clear()
                scope?.hiddenSettings?.also {
                    hiddenSettings.addAll(it)
                }
            },
            onConfirm = {
                unsaved = true
                showHiddenSettingsDialog = false
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(),
            contentPadding = paddingValues.withSafeHorizontal()
        ) {
            item {
                Card {
                    SwitchPreference(
                        checked = enabled,
                        onCheckedChange = {
                            unsaved = true
                            enabled = it
                        },
                        title = stringResourceBy("app_enable_hide")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card {
                    OverlayDropdownPreference(
                        title = stringResourceBy("work_mode"),
                        items = listOf(
                            stringResourceBy("blacklist"),
                            stringResourceBy("whitelist")
                        ),
                        selectedIndex = if (useWhitelist) 1 else 0,
                        onSelectedIndexChange = {
                            unsaved = true
                            applyTemplates.clear()
                            applyPresets.clear()
                            useWhitelist = it == 1
                        },
                        enabled = enabled
                    )

                    SwitchPreference(
                        title = stringResourceBy("app_exclude_system_apps"),
                        summary = stringResourceBy("app_exclude_system_apps_summary"),
                        checked = excludeSystemApps,
                        onCheckedChange = {
                            unsaved = true
                            excludeSystemApps = it
                        },
                        enabled = enabled && useWhitelist
                    )

                    SwitchPreference(
                        title = stringResourceBy("app_aggressive_filter"),
                        summary = stringResourceBy("app_aggressive_filter_summary"),
                        checked = aggressiveFilter,
                        onCheckedChange = {
                            unsaved = true
                            aggressiveFilter = it
                        },
                        enabled = enabled
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card {
                    ArrowPreference(
                        title = stringResourceBy("app_template_using", applyTemplates.size),
                        onClick = { showTemplateDialog = true },
                        enabled = enabled
                    )

                    AnimatedVisibility(
                        visible = !useWhitelist
                    ) {
                        ArrowPreference(
                            title = stringResourceFromModule(R.string.preset_using, applyPresets.size),
                            onClick = { showPresetDialog = true },
                            enabled = enabled
                        )
                    }

                    ArrowPreference(
                        title = if (useWhitelist) {
                            stringResourceBy("app_extra_apps_visible_count", extraApps.size)
                        } else {
                            stringResourceBy("app_extra_apps_invisible_count", extraApps.size)
                        },
                        onClick = dropUnlessStarted {
                            navigator.navigate(Route.AppSelector("extra_apps", extraApps))
                        },
                        enabled = enabled
                    )

                    ArrowPreference(
                        title = stringResourceFromModule(R.string.settings_hidden_count, hiddenSettings.size),
                        onClick = { showHiddenSettingsDialog = true },
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Stable
private object ScopeConfigState {
    var value by mutableStateOf(ConfigManager.scopes)

    fun isScopeEnabled(packageName: String): Boolean {
        return value.contains(packageName)
    }

    private operator fun <T : Map<String, HMAConfig.ScopeConfig>> State<T>.getValue(
        thisObj: Any?,
        property: KProperty<*>
    ): T {
        return value
    }

    private operator fun <T : Map<String, HMAConfig.ScopeConfig>> MutableState<T>.setValue(
        thisObj: Any?,
        property: KProperty<*>,
        value: T
    ) {
        this.value = value
        ConfigManager.scopes = value
    }
}