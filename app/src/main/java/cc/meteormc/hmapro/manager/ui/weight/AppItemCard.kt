package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.data.InstalledPackage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card

@Composable
fun AppItemCard(
    app: InstalledPackage,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .then(modifier),
        onClick = onClick,
        showIndication = onClick != null
    ) {
        BasicComponent(
            title = app.label,
            summary = app.packageName,
            modifier = Modifier,
            startAction = {
                Row {
                    Image(
                        painter = rememberDrawablePainter(app.icon),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(42.dp)
                            .alpha(if (enabled) 1f else 0.38f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }
            },
            enabled = enabled
        )
    }
}