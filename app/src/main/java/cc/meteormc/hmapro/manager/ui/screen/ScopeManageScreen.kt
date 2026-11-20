package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.preference.AppSortMethod
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.PaddingValuesSides
import cc.meteormc.hmapro.manager.ui.util.only
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.toMutableStateSet
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import cc.meteormc.hmapro.manager.ui.weight.AppItem
import cc.meteormc.hmapro.manager.ui.weight.AppList
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import cc.meteormc.hmapro.manager.ui.weight.CancelButton
import cc.meteormc.hmapro.manager.ui.weight.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.weight.EmptyContent
import cc.meteormc.hmapro.manager.ui.weight.PullToRefresh
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import kotlin.reflect.KProperty

@Composable
fun ScopeListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isInitial by rememberSaveable { mutableStateOf(true) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var installedPackages by remember { mutableStateOf(emptyList<InstalledPackage>()) }
    LaunchedEffect(Unit) {
        installedPackages = InstalledPackage.getInstalledPackages(
            packageManager = context.packageManager,
            scope = this
        )
        isInitial = false
    }

    val prefs = LocalPreferencesState.current
    val sortMethodMessage = stringResourceFromModule(R.string.sort_method)
    val reverseOrderMessage = stringResourceBy("list_reverse_order")
    val showSystemAppMessage = stringResourceBy("list_show_system")
    val byLabelMessage = stringResourceBy("list_sort_by_label")
    val byPackageMessage = stringResourceBy("list_sort_by_package_name")
    val byInstallTimeMessage = stringResourceBy("list_sort_by_install_time")
    val byUpdateTimeMessage = stringResourceBy("list_sort_by_update_time")
    val dropdownItems = remember(
        prefs.scopeSort,
        prefs.scopeReverseOrder,
        prefs.showSystemApp,
        sortMethodMessage,
        reverseOrderMessage,
        byLabelMessage,
        byPackageMessage,
        byInstallTimeMessage,
        byUpdateTimeMessage
    ) {
        val sort = prefs.scopeSort
        val reverseOrder = prefs.scopeReverseOrder
        val showSystemApp = prefs.showSystemApp
        listOf(
            DropdownItem(
                text = sortMethodMessage,
                children = listOf(
                    DropdownItem(
                        text = byLabelMessage,
                        selected = sort == AppSortMethod.BY_LABEL,
                        onClick = { prefs.scopeSort = AppSortMethod.BY_LABEL }
                    ),
                    DropdownItem(
                        text = byPackageMessage,
                        selected = sort == AppSortMethod.BY_PACKAGE_NAME,
                        onClick = { prefs.scopeSort = AppSortMethod.BY_PACKAGE_NAME }
                    ),
                    DropdownItem(
                        text = byInstallTimeMessage,
                        selected = sort == AppSortMethod.BY_INSTALL_TIME,
                        onClick = { prefs.scopeSort = AppSortMethod.BY_INSTALL_TIME }
                    ),
                    DropdownItem(
                        text = byUpdateTimeMessage,
                        selected = sort == AppSortMethod.BY_UPDATE_TIME,
                        onClick = { prefs.scopeSort = AppSortMethod.BY_UPDATE_TIME }
                    )
                )
            ),
            DropdownItem(
                text = reverseOrderMessage,
                selected = reverseOrder,
                onClick = { prefs.scopeReverseOrder = !reverseOrder }
            ),
            DropdownItem(
                text = showSystemAppMessage,
                selected = showSystemApp,
                onClick = { prefs.showSystemApp = !showSystemApp }
            )
        )
    }

    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val navigator = LocalNavigator.current
    val moduleStatus = LocalModuleStatus.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_app_manage"),
                navigationIcon = { BackButton() },
                actions = {
                    OverlayIconCascadingDropdownMenu(
                        entry = DropdownEntry(dropdownItems)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Tune,
                            contentDescription = "Control"
                        )
                    }
                }
            ) {
                SearchBar(
                    inputField = {
                        InputField(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = { expanded = false },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            label = stringResourceBy("search")
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp),
                    onExpandedChange = { expanded = it },
                    expanded = expanded,
                    outsideEndAction = {
                        CancelButton {
                            query = ""
                            expanded = false
                        }
                    },
                    content = {}
                )
            }
        }
    ) { paddingValues ->
        if (moduleStatus == ModuleStatus.ServiceNotRunning) {
            EmptyContent(
                text = stringResourceBy("home_xposed_service_off")
            )
            return@Scaffold
        }

        if (isInitial) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                InfiniteProgressIndicator()
            }
            return@Scaffold
        }

        val showSystemApp = prefs.showSystemApp
        val comparator = prefs.scopeSort.comparator.let {
            if (prefs.scopeReverseOrder) it.reversed() else it
        }
        val apps = installedPackages
            .filter {
                if (ScopeConfigState.isScopeEnabled(it.packageName)) return@filter true
                if (showSystemApp || !it.isSystemApp) return@filter true
                false
            }
            .filter {
                if (query.isBlank()) return@filter true
                if (it.label.contains(query, ignoreCase = true)) return@filter true
                if (it.packageName.contains(query, ignoreCase = true)) return@filter true
                false
            }
            .sortedWith(
                compareByDescending<InstalledPackage> {
                    ScopeConfigState.isScopeEnabled(it.packageName)
                }.then(comparator)
            )
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    installedPackages = InstalledPackage.getInstalledPackages(
                        packageManager = context.packageManager,
                        scope = this,
                        useCache = false
                    )
                    isRefreshing = false
                }
            },
            contentPadding = paddingValues
        ) {
            AppList(
                apps = apps,
                contentPadding = paddingValues.withSafeHorizontal()
            ) {
                AppItem(
                    app = it,
                    onClick = {
                        val nav = Route.ScopeSettings(it.label, it.packageName)
                        navigator.push(nav)
                    },
                    enabled = ScopeConfigState.isScopeEnabled(it.packageName)
                )
            }
        }
    }
}

@Composable
fun ScopeSettingsScreen(
    title: String,
    packageName: String
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = remember { ScopeConfigState.value[packageName] }
    var enabled by remember { mutableStateOf(scope != null) }
    var useWhitelist by remember { mutableStateOf(scope?.useWhitelist ?: false) }
    var excludeSystemApps by remember { mutableStateOf(scope?.excludeSystemApps ?: false) }
    var aggressiveFilter by remember { mutableStateOf(scope?.aggressiveFilter ?: false) }
    val applyTemplates = remember { scope?.applyTemplates.orEmpty().toMutableStateSet() }
    val extraAppList = remember { scope?.extraAppList.orEmpty().toMutableStateSet() }

    var unsaved by remember { mutableStateOf(false) }
    fun save() {
        if (!unsaved) return

        val newScopes = ScopeConfigState.value.toMutableMap()
        if (enabled) {
            newScopes[packageName] = HMAConfig.ScopeConfig.new().apply {
                this.aggressiveFilter = aggressiveFilter
                this.useWhitelist = useWhitelist
                this.excludeSystemApps = excludeSystemApps
                this.applyTemplates = applyTemplates
                this.extraAppList = extraAppList
            }
        } else {
            newScopes.remove(packageName)
        }

        ScopeConfigState.value = newScopes
        unsaved = false
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showExtraAppsBottomSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = unsaved) {
        showUnsavedDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = { BackButton(MiuixIcons.Close) },
                actions = {
                    IconButton(
                        onClick = {
                            save()
                            navigator.pop()
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
        OverlayDialog(
            show = showUnsavedDialog,
            title = stringResourceFromModule(R.string.unsaved_changes),
            onDismissRequest = { showUnsavedDialog = false }
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            DialogButtonPair(
                leftText = stringResourceFromModule(R.string.exit),
                rightText = stringResourceFromModule(R.string.save),
                onLeftClick = {
                    showUnsavedDialog = false
                    navigator.pop()
                },
                onRightClick = {
                    save()
                    showUnsavedDialog = false
                    navigator.pop()
                }
            )
        }

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
                        .verticalScrollEffect(enableHaptic = false)
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
                            }
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

        OverlayBottomSheet(
            show = showExtraAppsBottomSheet,
            title = stringResourceBy("title_app_select"),
            endAction = {
                Row {
                    IconButton(
                        onClick = { showExtraAppsBottomSheet = false }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = "Close"
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }
            },
            onDismissRequest = { showExtraAppsBottomSheet = false },
            insideMargin = DpSize(8.dp, 0.dp)
        ) {
            val scope = rememberCoroutineScope()
            var query by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            var apps by remember { mutableStateOf(emptyList<InstalledPackage>()) }

            LaunchedEffect(Unit) {
                apps = InstalledPackage.getInstalledPackages(
                    packageManager = context.packageManager,
                    scope = scope
                ).filter {
                    if (query.isBlank()) return@filter true
                    if (it.label.contains(query, ignoreCase = true)) return@filter true
                    if (it.packageName.contains(query, ignoreCase = true)) return@filter true
                    false
                }.sortedWith(
                    compareBy<InstalledPackage> { !extraAppList.contains(it.packageName) }.then(AppSortMethod.BY_LABEL.comparator)
                )
            }

            SearchBar(
                inputField = {
                    InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = stringResourceBy("search")
                    )
                },
                onExpandedChange = { expanded = it },
                expanded = expanded,
                content = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppList(
                apps = apps,
                contentPadding = paddingValues
                    .only(PaddingValuesSides.Bottom)
                    .withSafeHorizontal()
            ) {
                val pkg = it.packageName
                fun toggleExtraApp() {
                    if (extraAppList.contains(pkg)) {
                        extraAppList -= pkg
                    } else {
                        extraAppList += pkg
                    }
                }

                AppItem(
                    app = it,
                    onClick = { toggleExtraApp() }
                ) {
                    Checkbox(
                        state = ToggleableState(extraAppList.contains(pkg)),
                        onClick = { toggleExtraApp() }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(),
            contentPadding = paddingValues.withSafeHorizontal()
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

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
                            stringResourceBy("app_extra_apps_visible_count", extraAppList.size)
                        } else {
                            stringResourceBy("app_extra_apps_invisible_count", extraAppList.size)
                        },
                        onClick = { showExtraAppsBottomSheet = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
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
        value: T,
    ) {
        this.value = value
        ConfigManager.scopes = value
    }
}