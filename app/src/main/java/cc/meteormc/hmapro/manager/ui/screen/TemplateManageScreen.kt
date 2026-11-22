package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.preference.AppSortMethod
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.PaddingValuesSides
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.only
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import cc.meteormc.hmapro.manager.ui.weight.AppItem
import cc.meteormc.hmapro.manager.ui.weight.AppList
import cc.meteormc.hmapro.manager.ui.weight.BackButton
import cc.meteormc.hmapro.manager.ui.weight.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.weight.EmptyContent
import cc.meteormc.hmapro.manager.ui.weight.UnsavedChangesDialog
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.reflect.KProperty

@Composable
fun SharedTransitionScope.TemplateListScreen() {
    val navigator = LocalNavigator.current
    val templates = TemplateConfigState.value
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .containerTransform("template_manage"),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_template_manage"),
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.navigate(Route.NewTemplate)
                },
                modifier = Modifier.containerTransform("new_template")
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "New Template",
                    modifier = Modifier.size(32.dp),
                    tint = MiuixTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (templates.isEmpty()) {
            EmptyContent()
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(),
            contentPadding = paddingValues.withSafeHorizontal()
        ) {
            items(items = templates.entries.toList()) {
                val (name, template) = it
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    insideMargin = PaddingValues(16.dp),
                    showIndication = true,
                    onClick = {
                        navigator.navigate(Route.TemplateSettings(name))
                    }
                ) {
                    Row {
                        Text(
                            text = name,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = CircleShape,
                            color = MiuixTheme.colorScheme.tertiaryContainer
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResourceBy(
                                        if (template.isWhitelist) "whitelist" else "blacklist"
                                    ),
                                    color = MiuixTheme.colorScheme.onTertiaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedTransitionScope.TemplateSettingsScreen(name: String?) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val focusManager = LocalFocusManager.current
    val template = remember(name) {
        name?.let { TemplateConfigState.value[it] }
    }

    val isNew = name == null
    val isInvalid = !isNew && template == null
    LaunchedEffect(isInvalid) {
        if (isInvalid) {
            navigator.back()
        }
    }

    if (isInvalid) return
    var deleted by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(name ?: "") }
    var isWhitelist by remember(template) { mutableStateOf(template?.isWhitelist ?: false) }
    var targetApps by remember(template) { mutableStateOf(template?.appList ?: emptySet()) }
    val appliedApps = remember(ConfigManager.scopes, template) {
        ConfigManager.scopes.filterValues { name in it.applyTemplates }.keys
    }

    var unsaved by remember { mutableStateOf(false) }
    fun save() {
        if (!unsaved) return
        // todo: 添加nameInput的合法性检查

        val newTemplates = TemplateConfigState.value.toMutableMap()
        if (name != null) {
            newTemplates.remove(name)
        }

        if (!deleted) {
            newTemplates[nameInput] = HMAConfig.TemplateConfig.new().apply {
                this.isWhitelist = isWhitelist
                this.appList = targetApps
            }
        }

        TemplateConfigState.value = newTemplates
        unsaved = false
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAppliedAppsBottomSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = unsaved) {
        showUnsavedDialog = true
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isNew) Modifier.containerTransform("new_template")
                else Modifier
            ),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_template_settings"),
                navigationIcon = { BackButton(MiuixIcons.Close) },
                actions = {
                    IconButton(
                        onClick = {
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
            onDismiss = {
                showUnsavedDialog = false
                navigator.back()
            },
            onSave = { save() }
        )

        OverlayDialog(
            show = showDeleteConfirmDialog,
            title = stringResourceBy("template_delete"),
            onDismissRequest = { showDeleteConfirmDialog = false }
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            DialogButtonPair(
                leftText = stringResource(android.R.string.cancel),
                rightText = stringResource(android.R.string.ok),
                onLeftClick = {
                    showDeleteConfirmDialog = false
                },
                onRightClick = {
                    unsaved = true
                    deleted = true
                    save()
                    navigator.back()
                    showDeleteConfirmDialog = false
                }
            )
        }

        OverlayBottomSheet(
            show = showAppliedAppsBottomSheet,
            title = stringResourceBy("title_app_select"), //todo: 改名
            onDismissRequest = { showAppliedAppsBottomSheet = false }
        ) {
            if (appliedApps.isEmpty()) {
                EmptyContent(maxHeight = 300.dp)
                return@OverlayBottomSheet
            }

            val scope = rememberCoroutineScope()
            var apps by remember { mutableStateOf(emptyList<InstalledPackage>()) }
            LaunchedEffect(appliedApps) {
                apps = InstalledPackage.getInstalledPackages(
                    packageManager = context.packageManager,
                    scope = scope
                ).filter {
                    it.packageName in appliedApps
                }.sortedWith(
                    AppSortMethod.BY_LABEL.comparator
                )
            }

            AppList(
                apps = apps,
                modifier = Modifier.verticalScrollEffect(),
                contentPadding = paddingValues
                    .only(PaddingValuesSides.Bottom)
                    .withSafeHorizontal()
            ) {
                AppItem(
                    app = it,
                    onClick = {
                        navigator.navigate(Route.ScopeSettings(it.label, it.packageName))
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(width = 10.dp, height = 16.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = stringResourceBy("template_usage_hint"),
                        modifier = Modifier.padding(16.dp),
                        color = MiuixTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                @Suppress("DEPRECATION")
                TextField(
                    value = nameInput,
                    onValueChange = {
                        unsaved = true
                        nameInput = it
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MiuixTheme.colorScheme.surfaceContainer
                    ),
                    label = stringResourceBy("template_name"),
                    useLabelAsPlaceholder = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card {
                    // todo: 换一种表现方式
                    SwitchPreference(
                        title = stringResourceBy("whitelist"),
                        checked = isWhitelist,
                        onCheckedChange = {
                            unsaved = true
                            isWhitelist = it
                        }
                    )

                    ArrowPreference(
                        title = stringResourceBy("template_applied_count", appliedApps.size),
                        onClick = { showAppliedAppsBottomSheet = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isNew) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        onClick = { showDeleteConfirmDialog = true },
                        showIndication = true
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResourceBy("template_delete_title"),
                                modifier = Modifier.padding(16.dp),
                                color = MiuixTheme.colorScheme.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Stable
private object TemplateConfigState {
    var value by mutableStateOf(ConfigManager.templates)

    private operator fun <T : Map<String, HMAConfig.TemplateConfig>> State<T>.getValue(
        thisObj: Any?,
        property: KProperty<*>
    ): T {
        return value
    }

    private operator fun <T : Map<String, HMAConfig.TemplateConfig>> MutableState<T>.setValue(
        thisObj: Any?,
        property: KProperty<*>,
        value: T,
    ) {
        this.value = value
        ConfigManager.templates = value
    }
}