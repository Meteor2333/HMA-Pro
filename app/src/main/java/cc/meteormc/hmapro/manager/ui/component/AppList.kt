package cc.meteormc.hmapro.manager.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.data.InstalledPackage
import top.yukonga.miuix.kmp.basic.Card

@Composable
fun AppList(
    apps: List<InstalledPackage>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    item: @Composable ColumnScope.(InstalledPackage) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = apps) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                item(it)
            }
        }
    }
}