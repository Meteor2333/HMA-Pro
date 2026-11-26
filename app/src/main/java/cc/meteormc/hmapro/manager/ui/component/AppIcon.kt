package cc.meteormc.hmapro.manager.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun AppIcon(
    icon: Bitmap,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Image(
        bitmap = icon.asImageBitmap(),
        contentDescription = "App Icon",
        modifier = Modifier
            .size(42.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .then(modifier)
    )
}