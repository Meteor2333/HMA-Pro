package cc.meteormc.hmapro.manager.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun stringResourceBy(resName: String): String {
    return findResource(resName, "string") { resources, id -> resources.getString(id) }
}

@Composable
@ReadOnlyComposable
fun stringResourceFromModule(@StringRes id: Int): String {
    return LocalModuleResources.current.getString(id)
}

@Composable
fun stringResourceBy(resName: String, vararg formatArgs: Any): String {
    return findResource(resName, "string") { resources, id -> resources.getString(id, *formatArgs) }
}

@Composable
@ReadOnlyComposable
fun stringResourceFromModule(@StringRes id: Int, vararg formatArgs: Any): String {
    return LocalModuleResources.current.getString(id, *formatArgs)
}

@Composable
fun painterResourceBy(resName: String): Painter {
    return findResource(resName, "drawable") { _, id -> painterResource(id) }
}

@SuppressLint("DiscouragedApi")
inline fun <T> Context.findResource(
    resName: String,
    type: String,
    action: (Resources, Int) -> T
): T {
    val id = resources.getIdentifier(resName, type, packageName)
    if (id <= 0) {
        throw IllegalArgumentException("Resource not found: $resName")
    }

    return action(resources, id)
}

@Composable
private inline fun <T> findResource(
    resName: String,
    type: String,
    action: @Composable (Resources, Int) -> T
): T {
    return LocalContext.current.findResource(resName, type) { res, id -> action(res, id) }
}