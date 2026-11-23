package cc.meteormc.hmapro.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.state.ToggleableState
import androidx.navigation3.runtime.result.LocalResultEventBus
import cc.meteormc.hmapro.manager.ui.util.LocalNavigator
import cc.meteormc.hmapro.manager.ui.util.toMutableStateSet
import cc.meteormc.hmapro.manager.ui.weight.AppItem
import cc.meteormc.hmapro.manager.ui.weight.AppSelectorScaffold
import top.yukonga.miuix.kmp.basic.Checkbox

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
        AppItem(
            app = it,
            onClick = { toggle(it.packageName) }
        ) {
            Checkbox(
                state = ToggleableState(selected.contains(it.packageName)),
                onClick = { toggle(it.packageName) }
            )
        }
    }
}