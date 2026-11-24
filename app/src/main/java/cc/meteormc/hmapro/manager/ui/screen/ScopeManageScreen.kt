package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessStarted
import androidx.navigation3.runtime.result.ResultEffect
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.ui.component.AppIcon
import cc.meteormc.hmapro.manager.ui.component.AppSelectorScaffold
import cc.meteormc.hmapro.manager.ui.component.EmptyContent
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import cc.meteormc.hmapro.manager.ui.component.button.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.component.dialog.UnsavedChangesDialog
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
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
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
                        app = it,
                        enabled = enabled
                    )

                    Spacer(modifier = Modifier.width(5.dp))
                }
            },
            onClick = dropUnlessStarted {
                val nav = Route.ScopeSettings(it.label, it.packageName)
                navigator.navigate(nav)
            },
            enabled = enabled
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
    val extraApps = rememberSaveable(scope) { scope?.extraAppList.orEmpty().toMutableStateSet() }

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
                this.extraAppList = extraApps
            }
        } else {
            newScopes.remove(packageName)
        }

        ScopeConfigState.value = newScopes
        unsaved = false
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
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

        OverlayDialog(
            show = showTemplateDialog,
            title = stringResourceBy("app_choose_template"),
            largeScreen = true
        ) {
            val templates = ConfigManager.templates.filterValues { it.isWhitelist == useWhitelist }
            if (templates.isEmpty()) {
                EmptyContent(
                    maxHeight = 150.dp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScrollEffect(
                            enableHaptic = false,
                            enableOverScroll = false
                        )
                ) {
                    items(items = templates.keys.toList()) {
                        CheckboxPreference(
                            title = it,
                            checked = applyTemplates.contains(it),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    applyTemplates += it
                                } else {
                                    applyTemplates -= it
                                }
                            },
                            modifier = Modifier.squircleClip(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DialogButtonPair(
                leftText = stringResource(android.R.string.cancel),
                rightText = stringResource(android.R.string.ok),
                onLeftClick = {
                    showTemplateDialog = false
                    applyTemplates.clear()
                    scope?.applyTemplates?.also {
                        applyTemplates.addAll(it)
                    }
                },
                onRightClick = {
                    unsaved = true
                    showTemplateDialog = false
                }
            )
        }

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
                            useWhitelist = it == 1
                        }
                    )

                    SwitchPreference(
                        title = stringResourceBy("app_exclude_system_apps"),
                        summary = stringResourceBy("app_exclude_system_apps_summary"),
                        checked = excludeSystemApps,
                        onCheckedChange = {
                            unsaved = true
                            excludeSystemApps = it
                        },
                        enabled = useWhitelist
                    )

                    SwitchPreference(
                        title = stringResourceBy("app_aggressive_filter"),
                        summary = stringResourceBy("app_aggressive_filter_summary"),
                        checked = aggressiveFilter,
                        onCheckedChange = {
                            unsaved = true
                            aggressiveFilter = it
                        }
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
                        onClick = { showTemplateDialog = true }
                    )

                    ArrowPreference(
                        title = if (useWhitelist) {
                            stringResourceBy("app_extra_apps_visible_count", extraApps.size)
                        } else {
                            stringResourceBy("app_extra_apps_invisible_count", extraApps.size)
                        },
                        onClick = dropUnlessStarted {
                            navigator.navigate(Route.AppSelector("extra_apps", extraApps))
                        }
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