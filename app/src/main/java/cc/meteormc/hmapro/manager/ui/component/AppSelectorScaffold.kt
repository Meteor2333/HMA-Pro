package cc.meteormc.hmapro.manager.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.data.ModuleStatus
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import cc.meteormc.hmapro.manager.ui.component.button.CancelButton
import cc.meteormc.hmapro.manager.ui.preference.AppSortMethod
import cc.meteormc.hmapro.manager.ui.util.LocalModuleStatus
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.menu.OverlayIconCascadingDropdownMenu

@Composable
fun AppSelectorScaffold(
    modifier: Modifier = Modifier,
    title: String = stringResourceBy("title_app_select"),
    onBack: (() -> Unit)? = null,
    onRefresh: ((List<InstalledPackage>) -> Unit)? = null,
    filter: ((InstalledPackage) -> Boolean)? = null,
    enabled: (InstalledPackage) -> Boolean = { true },
    item: @Composable (InstalledPackage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reversion by rememberSaveable { mutableIntStateOf(0) }
    var isInitial by rememberSaveable { mutableStateOf(true) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var installedPackages by remember { mutableStateOf(emptyList<InstalledPackage>()) }
    suspend fun CoroutineScope.refreshPackages() {
        installedPackages = InstalledPackage.getInstalledPackages(
            packageManager = context.packageManager,
            scope = this,
            useCache = !isInitial
        ).let {
            if (filter != null) it.filter(filter) else it
        }
        onRefresh?.invoke(installedPackages)
    }

    LaunchedEffect(Unit) {
        refreshPackages()
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
        prefs.appSort,
        prefs.appReverseOrder,
        prefs.showSystemApp,
        sortMethodMessage,
        reverseOrderMessage,
        byLabelMessage,
        byPackageMessage,
        byInstallTimeMessage,
        byUpdateTimeMessage
    ) {
        val sort = prefs.appSort
        val reverseOrder = prefs.appReverseOrder
        val showSystemApp = prefs.showSystemApp
        listOf(
            DropdownItem(
                text = sortMethodMessage,
                children = listOf(
                    DropdownItem(
                        text = byLabelMessage,
                        selected = sort == AppSortMethod.BY_LABEL,
                        onClick = { prefs.appSort = AppSortMethod.BY_LABEL }
                    ),
                    DropdownItem(
                        text = byPackageMessage,
                        selected = sort == AppSortMethod.BY_PACKAGE_NAME,
                        onClick = { prefs.appSort = AppSortMethod.BY_PACKAGE_NAME }
                    ),
                    DropdownItem(
                        text = byInstallTimeMessage,
                        selected = sort == AppSortMethod.BY_INSTALL_TIME,
                        onClick = { prefs.appSort = AppSortMethod.BY_INSTALL_TIME }
                    ),
                    DropdownItem(
                        text = byUpdateTimeMessage,
                        selected = sort == AppSortMethod.BY_UPDATE_TIME,
                        onClick = { prefs.appSort = AppSortMethod.BY_UPDATE_TIME }
                    )
                )
            ),
            DropdownItem(
                text = reverseOrderMessage,
                selected = reverseOrder,
                onClick = { prefs.appReverseOrder = !reverseOrder }
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

    val moduleStatus = LocalModuleStatus.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = { BackButton(onBack = onBack) },
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
        val comparator = prefs.appSort.comparator.let {
            if (prefs.appReverseOrder) it.reversed() else it
        }
        val apps = remember(
            reversion,
            installedPackages,
            showSystemApp,
            query,
            comparator
        ) {
            installedPackages
                .filter {
                    if (enabled(it)) return@filter true
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
                    compareByDescending<InstalledPackage> { enabled(it) }.then(comparator)
                )
        }
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    refreshPackages()
                    isRefreshing = false
                    reversion++
                }
            },
            contentPadding = paddingValues
        ) {
            AppList(
                apps = apps,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollEffect(enableOverScroll = false),
                contentPadding = paddingValues.withSafeHorizontal()
            ) {
                item(it)
            }
        }
    }
}