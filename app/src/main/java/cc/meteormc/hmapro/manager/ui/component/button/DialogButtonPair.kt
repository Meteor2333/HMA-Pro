package cc.meteormc.hmapro.manager.ui.component.button

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun DialogButtonPair(
    leftText: String,
    rightText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Row {
        TextButton(
            text = leftText,
            onClick = onLeftClick,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(18.dp))

        TextButton(
            text = rightText,
            onClick = onRightClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColorsPrimary()
        )
    }
}