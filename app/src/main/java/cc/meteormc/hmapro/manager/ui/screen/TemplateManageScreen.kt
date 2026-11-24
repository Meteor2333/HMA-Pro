package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessStarted
import androidx.navigation3.runtime.result.ResultEffect
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.common.HMAConfig
import cc.meteormc.hmapro.manager.data.InstalledPackage
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.ui.component.AppIcon
import cc.meteormc.hmapro.manager.ui.component.AppList
import cc.meteormc.hmapro.manager.ui.component.EmptyContent
import cc.meteormc.hmapro.manager.ui.component.button.BackButton
import cc.meteormc.hmapro.manager.ui.component.button.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.component.dialog.UnsavedChangesDialog
import cc.meteormc.hmapro.manager.ui.nav.Route
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.PaddingValuesSides
import cc.meteormc.hmapro.manager.ui.util.containerTransform
import cc.meteormc.hmapro.manager.ui.util.isDarkMode
import cc.meteormc.hmapro.manager.ui.util.only
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.toMutableStateSet
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
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
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
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
                onClick = dropUnlessStarted {
                    navigator.navigate(Route.NewTemplate)
                },
                modifier = Modifier.containerTransform(
                    containerKey = "new_template",
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(150)),
                    resizeMode = ResizeMode.scaleToBounds(ContentScale.Fit)
                )
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
                    onClick = dropUnlessStarted {
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
    var deleted by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf(name ?: "") }
    var isWhitelist by rememberSaveable(template) { mutableStateOf(template?.isWhitelist ?: false) }
    val targetApps = rememberSaveable(template) { template?.appList.orEmpty().toMutableStateSet() }
    val appliedApps = rememberSaveable(ConfigManager.scopes, template) {
        ConfigManager.scopes
            .filterValues { name in it.applyTemplates }
            .keys
            .toMutableStateSet()
    }

    var exists by rememberSaveable { mutableStateOf(false) }
    var isNameBlank by rememberSaveable { mutableStateOf(false) }
    fun check(): Boolean {
        if (nameInput.isBlank()) {
            isNameBlank = true
            return false
        }

        if ((name == null || name != nameInput) && nameInput in TemplateConfigState.value) {
            exists = true
            return false
        }

        return true
    }

    var unsaved by rememberSaveable { mutableStateOf(false) }
    fun save(): Boolean {
        if (!unsaved) return true
        if (!check()) return false

        val newTemplates = TemplateConfigState.value.toMutableMap()
        if (name != null) {
            newTemplates.remove(name)
        }

        if (deleted) {
            for (scope in ConfigManager.scopes.values) {
                if (name == null) continue
                if (name !in scope.applyTemplates) continue
                val applyTemplates = scope.applyTemplates.toMutableSet()
                applyTemplates.remove(name)
                scope.applyTemplates = applyTemplates
            }
        } else {
            newTemplates[nameInput] = HMAConfig.TemplateConfig.new().apply {
                this.isWhitelist = isWhitelist
                this.appList = targetApps
            }
        }

        TemplateConfigState.value = newTemplates
        unsaved = false
        return true
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAppliedAppsBottomSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = unsaved) {
        if (check()) {
            showUnsavedDialog = true
        }
    }

    ResultEffect<Set<String>>("target_apps") {
        if (targetApps != it) {
            unsaved = true
            targetApps.clear()
            targetApps.addAll(it)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isNew) Modifier.containerTransform(
                    containerKey = "new_template",
                    exit = fadeOut(tween(150))
                )
                else Modifier
            ),
        topBar = {
            SmallTopAppBar(
                title = stringResourceBy("title_template_settings"),
                navigationIcon = { BackButton(MiuixIcons.Close) },
                actions = {
                    IconButton(
                        onClick = dropUnlessStarted {
                            if (!save()) return@dropUnlessStarted
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
        OverlayDialog(
            show = exists,
            title = stringResourceBy("template_name_invalid"),
            summary = stringResourceBy("template_name_already_exist"),
            onDismissRequest = { exists = false }
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            DialogButtonPair(
                leftText = stringResourceFromModule(R.string.exit),
                rightText = stringResource(android.R.string.cancel),
                onLeftClick = dropUnlessStarted { navigator.back() },
                onRightClick = { exists = false }
            )
        }

        OverlayDialog(
            show = isNameBlank,
            title = stringResourceBy("template_name_invalid"),
            summary = stringResourceFromModule(R.string.template_name_blank),
            onDismissRequest = { isNameBlank = false }
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            DialogButtonPair(
                leftText = stringResourceFromModule(R.string.exit),
                rightText = stringResource(android.R.string.cancel),
                onLeftClick = dropUnlessStarted { navigator.back() },
                onRightClick = { isNameBlank = false }
            )
        }

        UnsavedChangesDialog(
            show = showUnsavedDialog,
            onDismiss = { showUnsavedDialog = false },
            onExit = dropUnlessStarted { navigator.back() },
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
                onRightClick = dropUnlessStarted {
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
            title = stringResourceFromModule(R.string.view_apps),
            onDismissRequest = { showAppliedAppsBottomSheet = false },
            insideMargin = DpSize.Zero
        ) {
            if (appliedApps.isEmpty()) {
                EmptyContent(maxHeight = 300.dp)
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                return@OverlayBottomSheet
            }

            val scope = rememberCoroutineScope()
            var apps by remember { mutableStateOf(emptyList<InstalledPackage>()) }
            LaunchedEffect(Unit) {
                apps = InstalledPackage.getInstalledPackages(
                    packageManager = context.packageManager,
                    scope = scope
                )
            }

            AppList(
                apps = apps.filter { it.packageName in appliedApps },
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScrollEffect(),
                contentPadding = paddingValues
                    .only(PaddingValuesSides.Bottom)
                    .withSafeHorizontal()
            ) {
                ArrowPreference(
                    title = it.label,
                    summary = it.packageName,
                    startAction = {
                        Row {
                            AppIcon(app = it)

                            Spacer(modifier = Modifier.width(5.dp))
                        }
                    },
                    onClick = dropUnlessStarted {
                        navigator.navigate(Route.ScopeSettings(it.label, it.packageName))
                    }
                )
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
                        color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                    OverlayDropdownPreference(
                        title = stringResourceFromModule(R.string.template_type),
                        items = listOf(
                            stringResourceBy("blacklist"),
                            stringResourceBy("whitelist")
                        ),
                        selectedIndex = if (isWhitelist) 1 else 0,
                        onSelectedIndexChange = {
                            unsaved = true
                            isWhitelist = it == 1
                        }
                    )

                    ArrowPreference(
                        title = stringResourceBy(
                            if (isWhitelist) "template_apps_visible_count"
                            else "template_apps_invisible_count",
                            targetApps.size
                        ),
                        onClick = dropUnlessStarted {
                            navigator.navigate(Route.AppSelector("target_apps", targetApps))
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
                            color = when {
                                MiuixTheme.isDynamicColor -> MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                isDarkMode() -> Color(0xFF2E0603)
                                else -> Color(0xFFFAE8E3)
                            }
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
        value: T
    ) {
        this.value = value
        ConfigManager.templates = value
    }
}