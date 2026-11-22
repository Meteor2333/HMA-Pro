package cc.meteormc.hmapro.manager.ui.weight

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.R
import cc.meteormc.hmapro.manager.ui.util.stringResourceFromModule
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
fun UnsavedChangesDialog(
    show: Boolean,
    onDismiss: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    OverlayDialog(
        show = show,
        title = stringResourceFromModule(R.string.unsaved_changes),
        onDismissRequest = onDismiss
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        DialogButtonPair(
            leftText = stringResourceFromModule(R.string.exit),
            rightText = stringResourceFromModule(R.string.save),
            onLeftClick = onDismiss,
            onRightClick = {
                onSave()
                onDismiss()
            }
        )
    }
}