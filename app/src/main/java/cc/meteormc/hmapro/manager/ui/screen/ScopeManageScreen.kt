package cc.meteormc.hmapro.manager.ui.screen

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.preference.AppSortMethod
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.copy
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.weight.AppItemCard
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import cc.meteormc.hmapro.manager.ui.weight.PullToRefresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ScopeListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var installedPackages by remember { mutableStateOf(emptyList<InstalledPackage>()) }
    suspend fun refreshPackages() {
        val packageManager = context.packageManager
        installedPackages = withContext(Dispatchers.IO) {
            val packages = ServiceClient.getInstalledPackages() ?: return@withContext emptyList()
            packages.mapNotNull {
                val appInfo = it.applicationInfo ?: return@mapNotNull null
                InstalledPackage(
                    label = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.loadUnbadgedIcon(packageManager),
                    packageName = it.packageName,
                    isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                    firstInstallTime = it.firstInstallTime,
                    lastUpdateTime = it.lastUpdateTime
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPackages()
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
            )
        }
    ) { paddingValues ->
        if (moduleStatus == ModuleStatus.ServiceNotRunning) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResourceBy("home_xposed_service_off"),
                    color = MiuixTheme.colorScheme.onSecondary,
                    fontSize = 18.sp
                )
            }
            return@Scaffold
        }

        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    refreshPackages()
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollEffect(enableOverScroll = false),
                contentPadding = paddingValues.copy(top = 0.dp)
            ) {
                val comparator = prefs.scopeSort.comparator
                val showSystemApp = prefs.showSystemApp
                val items = installedPackages
                    .filter { showSystemApp || !it.isSystemApp }
                    .sortedWith(
                        compareByDescending<InstalledPackage> { ConfigManager.isScopeEnabled(it.packageName) }.then(comparator)
                    )
                    .let {
                        if (prefs.scopeReverseOrder) it.reversed() else it
                    }
                items(
                    items = items,
                    key = { it.packageName }
                ) {
                    AppItemCard(
                        app = it,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            val nav = Route.ScopeSettings(it.label, it.packageName)
                            navigator.push(nav)
                        },
                        enabled = ConfigManager.isScopeEnabled(it.packageName)
                    )
                }
            }
        }
    }
}

@Composable
fun ScopeSettingsScreen(
    title: String,
    packageName: String
) {
    val scope = remember { ConfigManager.scopes[packageName] }
    var enabled by remember { mutableStateOf(scope != null) }
    var useWhitelist by remember { mutableStateOf(scope?.useWhitelist ?: false) }
    var excludeSystemApps by remember { mutableStateOf(scope?.excludeSystemApps ?: false) }
    var aggressiveFilter by remember { mutableStateOf(scope?.aggressiveFilter ?: false) }
    var applyTemplates by remember { mutableStateOf(scope?.applyTemplates.orEmpty()) }
    var extraAppList by remember { mutableStateOf(scope?.extraAppList.orEmpty()) }

    val navigator = LocalNavigator.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = { BackButton(MiuixIcons.Close) },
                actions = {
                    IconButton(
                        onClick = {
                            val newScopes = ConfigManager.scopes.toMutableMap()
                            newScopes[packageName] = HMAConfig.ScopeConfig.new().apply {
                                this.aggressiveFilter = aggressiveFilter
                                this.useWhitelist = useWhitelist
                                this.excludeSystemApps = excludeSystemApps
                                this.applyTemplates = applyTemplates
                                this.extraAppList = extraAppList
                            }
                            ConfigManager.scopes = newScopes
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScrollEffect(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            item {
                SwitchPreference(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    title = stringResourceBy("app_enable_hide")
                )
            }

            item {
                OverlayDropdownPreference(
                    title = stringResourceBy("work_mode"),
                    items = listOf(
                        stringResourceBy("blacklist"),
                        stringResourceBy("whitelist")
                    ),
                    selectedIndex = if (useWhitelist) 1 else 0,
                    onSelectedIndexChange = { useWhitelist = it == 1 }
                )
            }

            item {
                SwitchPreference(
                    title = stringResourceBy("app_exclude_system_apps"),
                    summary = stringResourceBy("app_exclude_system_apps_summary"),
                    checked = excludeSystemApps,
                    onCheckedChange = { excludeSystemApps = it },
                    enabled = useWhitelist
                )
            }

            item {
                SwitchPreference(
                    title = stringResourceBy("app_aggressive_filter"),
                    summary = stringResourceBy("app_aggressive_filter_summary"),
                    checked = aggressiveFilter,
                    onCheckedChange = { aggressiveFilter = it }
                )
            }

            item {
                ArrowPreference(
                    title = stringResourceBy("app_template_using", applyTemplates.size),
                    onClick = {
                        // todo
                    }
                )
            }

            item {
                ArrowPreference(
                    title = if (useWhitelist) {
                        stringResourceBy("app_extra_apps_visible_count", extraAppList.size)
                    } else {
                        stringResourceBy("app_extra_apps_invisible_count", extraAppList.size)
                    },
                    onClick = {
                        // todo
                    }
                )
            }
        }
    }
}