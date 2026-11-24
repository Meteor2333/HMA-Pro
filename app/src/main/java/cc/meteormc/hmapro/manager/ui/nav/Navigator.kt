package cc.meteormc.hmapro.manager.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

class Navigator(val backStack: NavBackStack<NavKey>) {
    fun navigate(key: NavKey) {
        backStack.add(key)
    }

    fun back(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun replace(key: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }
}

@Composable
fun rememberNavigator(vararg elements: NavKey): Navigator {
    val backStack = rememberNavBackStack(*elements)
    return remember(backStack) { Navigator(backStack) }
}