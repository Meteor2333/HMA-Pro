package cc.meteormc.hmapro.manager.ui.page

import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.DexFields
import cc.meteormc.hmapro.HmaProModule
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.service.ConfigManager
import cc.meteormc.hmapro.manager.service.ServiceClient
import cc.meteormc.hmapro.manager.ui.util.LocalPreferencesState
import cc.meteormc.hmapro.manager.ui.util.copy
import cc.meteormc.hmapro.manager.ui.util.rememberSnackbarHostState
import cc.meteormc.hmapro.manager.ui.util.restart
import cc.meteormc.hmapro.manager.ui.util.stringResourceBy
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import cc.meteormc.hmapro.manager.ui.util.withSafeHorizontal
import cc.meteormc.hmapro.manager.ui.component.button.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.component.PageTopAppBar
import cc.meteormc.hmapro.manager.util.ConfigBackuper
import cc.meteormc.hmapro.manager.util.Shell
import cc.meteormc.hmapro.manager.util.SystemProperties
import cc.meteormc.xposedkit.get
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import java.util.Locale

private const val APP_DATA_ISOLATION_PROPERTY = "persist.zygote.app_data_isolation"
private const val VOLD_APP_DATA_ISOLATION_PROPERTY = "persist.sys.vold_app_data_isolation_enabled"

@Composable
fun SettingsPage(outerPadding: PaddingValues) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val prefs = LocalPreferencesState.current
    val uriHandler = LocalUriHandler.current

    val followSystemMessage = stringResourceBy("follow_system")
    val languageOptions = remember(followSystemMessage) {
        DexFields.LangList_LOCALES.get<Array<String>>(null).map {
            val name = if (it == "SYSTEM") {
                followSystemMessage
            } else {
                val locale = Locale.forLanguageTag(it)
                locale.getDisplayName(locale)
            }
            it to name
        }
    }
    val themeOptions = listOf(
        ColorSchemeMode.System to followSystemMessage,
        ColorSchemeMode.Light to stringResourceFromModule(R.string.theme_light),
        ColorSchemeMode.Dark to stringResourceFromModule(R.string.theme_dark)
    )
    val logSizeOptions = listOf(
        128 to "128KiB",
        256 to "256KiB",
        512 to "512KiB",
        1024 to "1MiB"
    )

    fun isMonet(mode: ColorSchemeMode): Boolean {
        return when (mode) {
            ColorSchemeMode.MonetSystem,
            ColorSchemeMode.MonetLight,
            ColorSchemeMode.MonetDark -> true
            else -> false
        }
    }

    fun useMonet(mode: ColorSchemeMode, useMonet: Boolean): ColorSchemeMode {
        return when (mode) {
            ColorSchemeMode.System, ColorSchemeMode.MonetSystem -> {
                if (useMonet) ColorSchemeMode.MonetSystem
                else ColorSchemeMode.System
            }
            ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> {
                if (useMonet) ColorSchemeMode.MonetLight
                else ColorSchemeMode.Light
            }
            ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> {
                if (useMonet) ColorSchemeMode.MonetDark
                else ColorSchemeMode.Dark
            }
        }
    }

    var detailLog by remember { mutableStateOf(ConfigManager.detailLog) }
    var maxLogSize by remember { mutableIntStateOf(ConfigManager.maxLogSize) }
    var forceMountData by remember { mutableStateOf(ConfigManager.forceMountData) }
    var appDataIsolation by remember {
        mutableStateOf(SystemProperties.getBoolean(APP_DATA_ISOLATION_PROPERTY, true))
    }
    var voldAppDataIsolation by remember {
        mutableStateOf(SystemProperties.getBoolean(VOLD_APP_DATA_ISOLATION_PROPERTY, false))
    }

    val scope = rememberCoroutineScope()
    val snackbarState = rememberSnackbarHostState()
    val rebootMessage = stringResourceBy("settings_need_reboot")
    val deniedMessage = stringResourceBy("settings_permission_denied")
    var showAppDataIsolationConfirmDialog by remember { mutableStateOf(false) }
    var showVoldAppDataIsolationConfirmDialog by remember { mutableStateOf(false) }
    var showStopSystemServiceConfirmDialog by remember { mutableStateOf(false) }
    var showCleanEnvConfirmDialog by remember { mutableStateOf(false) }
    ConfirmDialog(
        show = showAppDataIsolationConfirmDialog,
        onCancel = { showAppDataIsolationConfirmDialog = false },
        onConfirm = {
            val newValue = !appDataIsolation
            scope.launch {
                val isSuccess = SystemProperties.set(APP_DATA_ISOLATION_PROPERTY, if (newValue) "1" else "0")
                if (isSuccess) {
                    appDataIsolation = newValue
                    showAppDataIsolationConfirmDialog = false
                    snackbarState.showSnackbar(
                        message = rebootMessage,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                } else {
                    Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    ConfirmDialog(
        show = showVoldAppDataIsolationConfirmDialog,
        onCancel = { showVoldAppDataIsolationConfirmDialog = false },
        onConfirm = {
            val newValue = !voldAppDataIsolation
            scope.launch {
                val isSuccess = SystemProperties.set(VOLD_APP_DATA_ISOLATION_PROPERTY, if (newValue) "1" else "0")
                if (isSuccess) {
                    voldAppDataIsolation = newValue
                    showVoldAppDataIsolationConfirmDialog = false
                    snackbarState.showSnackbar(
                        message = rebootMessage,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                } else {
                    Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    StopSystemServiceConfirmDialog(
        show = showStopSystemServiceConfirmDialog,
        onDismiss = { showStopSystemServiceConfirmDialog = false }
    )
    CleanEnvConfirmDialog(
        show = showCleanEnvConfirmDialog,
        onDismiss = { showCleanEnvConfirmDialog = false }
    )

    val exportSuccessfulMessage = stringResourceBy("home_exported")
    val exportFailedMessage = stringResourceBy("home_export_failed")
    val importSuccessfulMessage = stringResourceBy("home_import_successful")
    val importFailedMessage = stringResourceBy("home_import_failed")
    val backupSAFLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        scope.launch {
            val isSuccess = ConfigBackuper.backup(context, it)
            if (isSuccess) {
                Toast.makeText(
                    context,
                    exportSuccessfulMessage,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    exportFailedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    val restoreSAFLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        scope.launch {
            val isSuccess = ConfigBackuper.restore(context, it)
            if (isSuccess) {
                Toast.makeText(
                    context,
                    importSuccessfulMessage,
                    Toast.LENGTH_SHORT
                ).show()
                activity!!.recreate()
            } else {
                Toast.makeText(
                    context,
                    importFailedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PageTopAppBar(
                title = stringResourceBy("title_settings"),
                padding = outerPadding,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollEffect(scrollBehavior = scrollBehavior),
            contentPadding = outerPadding
                .withSafeHorizontal()
                .copy(top = innerPadding.calculateTopPadding())
        ) {
            item {
                SettingsCard(title = stringResourceBy("settings_language")) {
                    OverlayDropdownPreference(
                        title = stringResourceBy("settings_language"),
                        items = languageOptions.map { it.second },
                        selectedIndex = languageOptions.indexOfFirst { it.first == prefs.language },
                        onSelectedIndexChange = {
                            prefs.language = languageOptions[it].first
                            activity!!.recreate()
                        }
                    )

                    ArrowPreference(
                        title = stringResourceBy("settings_translate"),
                        summary = stringResourceBy("settings_translate_summary", stringResourceBy("app_name")),
                        onClick = {
                            uriHandler.openUri("https://crowdin.com/project/hide-my-applist")
                        }
                    )
                }
            }

            item {
                SettingsCard(title = stringResourceBy("settings_theme")) {
                    OverlayDropdownPreference(
                        title = stringResourceBy("settings_theme"),
                        items = themeOptions.map { it.second },
                        selectedIndex = themeOptions.indexOfFirst { it.first == useMonet(prefs.colorMode, false) },
                        onSelectedIndexChange = { prefs.colorMode = useMonet(themeOptions[it].first, isMonet(prefs.colorMode)) }
                    )

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.use_monet),
                        checked = isMonet(prefs.colorMode),
                        onCheckedChange = { prefs.colorMode = useMonet(prefs.colorMode, it) }
                    )

                    AnimatedVisibility(
                        visible = isMonet(prefs.colorMode)
                    ) {
                        Column {
                            OverlayDropdownPreference(
                                title = stringResourceFromModule(R.string.color_spec_title),
                                summary = stringResourceFromModule(R.string.color_spec_summary),
                                items = ThemeColorSpec.entries.map { it.name },
                                selectedIndex = ThemeColorSpec.entries.indexOfFirst { it == prefs.colorSpec },
                                onSelectedIndexChange = { prefs.colorSpec = ThemeColorSpec.entries[it] }
                            )

                            OverlayDropdownPreference(
                                title = stringResourceFromModule(R.string.palette_style_title),
                                summary = stringResourceFromModule(R.string.palette_style_summary),
                                items = ThemePaletteStyle.entries.map { it.name },
                                selectedIndex = ThemePaletteStyle.entries.indexOfFirst { it == prefs.paletteStyle },
                                onSelectedIndexChange = { prefs.paletteStyle = ThemePaletteStyle.entries[it] }
                            )
                        }
                    }

                    if (isRuntimeShaderSupported()) {
                        SwitchPreference(
                            title = stringResourceFromModule(R.string.enable_blur_title),
                            checked = prefs.enableBlur,
                            onCheckedChange = { prefs.enableBlur = it }
                        )
                    }

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.enable_swipe_back_gesture_title),
                        summary = stringResourceFromModule(R.string.enable_swipe_back_gesture_summary),
                        checked = prefs.enableSwipeDismiss,
                        onCheckedChange = { prefs.enableSwipeDismiss = it }
                    )

                    SwitchPreference(
                        title = stringResourceFromModule(R.string.modern_ui_title),
                        summary = stringResourceFromModule(R.string.modern_ui_summary),
                        checked = prefs.modernUi,
                        onCheckedChange = {
                            prefs.modernUi = it
                            activity!!.restart()
                        }
                    )
                }
            }

            item {
                SettingsCard(title = stringResourceBy("settings_module")) {
                    SwitchPreference(
                        title = stringResourceBy("settings_detail_log"),
                        checked = detailLog,
                        onCheckedChange = {
                            detailLog = it
                            ConfigManager.detailLog = detailLog
                        }
                    )

                    OverlayDropdownPreference(
                        title = stringResourceBy("settings_max_log_size"),
                        items = logSizeOptions.map { it.second },
                        selectedIndex = logSizeOptions.indexOfFirst { it.first == maxLogSize },
                        onSelectedIndexChange = {
                            maxLogSize = logSizeOptions[it].first
                            ConfigManager.maxLogSize = maxLogSize
                        }
                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_hide_icon"),
                        summary = stringResourceBy("settings_hide_icon_summary"),
                        checked = prefs.hideIcon,
                        onCheckedChange = {
                            prefs.hideIcon = it
                            context.packageManager.setComponentEnabledSetting(
                                ComponentName(
                                    context,
                                    HmaProModule.HMA_PACKAGE + ".MainActivityLauncher"
                                ),
                                if (it) {
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                } else {
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                },
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_disable_update"),
                        checked = prefs.disableUpdate,
                        onCheckedChange = { prefs.disableUpdate = it }
                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_force_mount_data"),
                        summary = stringResourceBy("settings_force_mount_data_summary"),
                        checked = forceMountData,
                        onCheckedChange = {
                            forceMountData = it
                            ConfigManager.forceMountData = it
                        }
                    )
                }
            }

            item {
                SettingsCard(title = stringResourceBy("system")) {
                    SwitchPreference(
                        title = stringResourceBy("settings_app_data_isolation"),
                        summary = stringResourceBy("settings_change_require_root"),
                        checked = appDataIsolation,
                        onCheckedChange = { showAppDataIsolationConfirmDialog = true }
                    )

                    SwitchPreference(
                        title = stringResourceBy("settings_vold_app_data_isolation"),
                        summary = stringResourceBy("settings_change_require_root"),
                        checked = voldAppDataIsolation,
                        onCheckedChange = { showVoldAppDataIsolationConfirmDialog = true }
                    )

                    ArrowPreference(
                        title = stringResourceBy("settings_stop_system_service"),
                        summary = stringResourceBy("settings_stop_system_service_summary"),
                        onClick = { showStopSystemServiceConfirmDialog = true }
                    )

                    ArrowPreference(
                        title = stringResourceBy("settings_force_clean_env"),
                        summary = stringResourceBy("settings_force_clean_env_summary"),
                        onClick = { showCleanEnvConfirmDialog = true }
                    )
                }
            }

            item {
                SettingsCard(title = stringResourceBy("home_backup_and_restore")) {
                    ArrowPreference(
                        title = stringResourceBy("home_backup_config"),
                        onClick = { backupSAFLauncher.launch("HMA_Config.json") }
                    )

                    ArrowPreference(
                        title = stringResourceBy("home_restore_config"),
                        onClick = { restoreSAFLauncher.launch("application/json") }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    SmallTitle(
        text = title,
        insideMargin = PaddingValues(16.dp, 8.dp)
    )

    Card(content = content)

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ConfirmDialog(
    show: Boolean,
    title: String? = stringResourceBy("settings_warning"),
    summary: String? = stringResourceFromModule(R.string.confirm_action),
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    OverlayDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onCancel
    ) {
        DialogButtonPair(
            leftText = stringResource(android.R.string.cancel),
            rightText = stringResource(android.R.string.ok),
            onLeftClick = onCancel,
            onRightClick = onConfirm
        )
    }
}

@Composable
private fun StopSystemServiceConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    var showCleanEnvDialog by remember { mutableStateOf(false) }
    ConfirmDialog(
        show = show,
        onCancel = onDismiss,
        onConfirm = {
            onDismiss()
            showCleanEnvDialog = true
        }
    )

    val context = LocalContext.current
    val toastMessage = stringResourceBy("settings_stop_system_service")
    val offMessage = stringResourceBy("home_xposed_service_off")
    fun stopService(cleanEnv: Boolean) {
        if (ServiceClient.getServiceVersion() == 0) {
            Toast.makeText(context,  offMessage, Toast.LENGTH_SHORT).show()
            return
        }

        ServiceClient.stopService(cleanEnv)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    OverlayDialog(
        show = showCleanEnvDialog,
        title = stringResourceBy("settings_is_clean_env"),
        summary = stringResourceBy("settings_is_clean_env_summary"),
        onDismissRequest = { showCleanEnvDialog = false }
    ) {
        DialogButtonPair(
            leftText = stringResourceBy("no"),
            rightText = stringResourceBy("yes"),
            onLeftClick = {
                showCleanEnvDialog = false
                stopService(false)
            },
            onRightClick = {
                showCleanEnvDialog = false
                stopService(true)
            }
        )
    }
}

@Composable
private fun CleanEnvConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val successfulMessage = stringResourceBy("settings_force_clean_env_toast_successful")
    val deniedMessage = stringResourceBy("settings_permission_denied")
    ConfirmDialog(
        show = show,
        title = stringResourceBy("settings_force_clean_env"),
        summary = stringResourceBy("settings_is_clean_env_summary"),
        onCancel = onDismiss,
        onConfirm = {
            onDismiss()
            Shell.exec(
                "rm -rf /data/misc/systemkeys/hide_my_applist* /data/misc/hide_my_applist* /data/system/hide_my_applist*",
                root = true
            ) {
                if (it.isSuccess) {
                    Toast.makeText(context, successfulMessage, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}