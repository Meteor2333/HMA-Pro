package cc.meteormc.hmapro.manager.ui.nav

import java.util.Collections

object NavMetadata {
    val DISABLE_TRANSITION = "cc.meteormc.hmapro.disable_transition" with true

    val ENABLE_SWIPE_DISMISS = "cc.meteormc.hmapro.enable_swipe_dismiss" with true

    fun Map<String, Any>.has(key: Map<String, Any>): Boolean {
        return key.size == 1 && this.containsKey(key.keys.first())
    }

    private infix fun <T> String.with(value: T): Map<String, T> {
        return Collections.singletonMap(this, value)
    }
}