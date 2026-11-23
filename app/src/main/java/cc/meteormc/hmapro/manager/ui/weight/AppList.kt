package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.data.InstalledPackage
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppList(
    apps: List<InstalledPackage>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    item: @Composable LazyItemScope.(InstalledPackage) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = apps) {
            item(it)
        }
    }
}

@Composable
fun AppItem(
    app: InstalledPackage,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    endActions: @Composable (RowScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        onClick = { onClick?.invoke() },
        showIndication = onClick != null
    ) {
        BasicComponent(
            startAction = {
                Row {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(42.dp)
                            .alpha(if (enabled) 1f else 0.38f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }
            },
            endActions = endActions,
            enabled = enabled
        ) {
            Text(
                text = app.label,
                modifier = Modifier.basicMarquee(),
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurface
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                fontWeight = FontWeight.Medium,
                minLines = 1
            )
            Text(
                text = app.packageName,
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
                fontSize = MiuixTheme.textStyles.body2.fontSize
            )
        }
    }
}