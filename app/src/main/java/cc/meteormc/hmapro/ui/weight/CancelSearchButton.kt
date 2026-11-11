package cc.meteormc.hmapro.ui.weight

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CancelSearchButton(
    onClick: () -> Unit
) {
    Text(
        text = stringResource(android.R.string.cancel),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                start = 6.dp,
                end = 18.dp
            ),
        color = MiuixTheme.colorScheme.primary
    )
}