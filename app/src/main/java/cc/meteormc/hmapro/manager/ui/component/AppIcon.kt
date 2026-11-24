package cc.meteormc.hmapro.manager.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.data.InstalledPackage

@Composable
fun AppIcon(
    app: InstalledPackage,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Image(
        bitmap = app.icon.asImageBitmap(),
        contentDescription = "App Icon",
        modifier = Modifier
            .size(42.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .then(modifier)
    )
}