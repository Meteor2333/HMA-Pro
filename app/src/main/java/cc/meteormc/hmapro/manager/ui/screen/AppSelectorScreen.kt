package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.result.LocalResultEventBus
import cc.meteormc.hmapro.manager.ui.component.AppIcon
import cc.meteormc.hmapro.manager.ui.component.AppSelectorScaffold
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.toMutableStateSet
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference

@Composable
fun AppSelectorScreen(
    resultKey: String,
    selected: Set<String>
) {
    val navigator = LocalNavigator.current
    val resultBus = LocalResultEventBus.current
    val selected = remember(selected) { selected.toMutableStateSet() }

    fun onBack() {
        resultBus.sendResult(
            resultKey = resultKey,
            result = selected.toSet()
        )
    }

    fun toggle(packageName: String) {
        if (selected.contains(packageName)) {
            selected -= packageName
        } else {
            selected += packageName
        }
    }

    BackHandler {
        onBack()
        navigator.back()
    }

    AppSelectorScaffold(
        onBack = { onBack() },
        onRefresh = { selected.retainAll(it.map { app -> app.packageName }) },
        enabled = { selected.contains(it.packageName) }
    ) {
        CheckboxPreference(
            title = it.label,
            summary = it.packageName,
            checked = selected.contains(it.packageName),
            onCheckedChange = { checked ->
                if (checked) {
                    selected += it.packageName
                } else {
                    selected -= it.packageName
                }
            },
            startAction = {
                AppIcon(app = it)
            },
            checkboxLocation = CheckboxLocation.End
        )
    }
}