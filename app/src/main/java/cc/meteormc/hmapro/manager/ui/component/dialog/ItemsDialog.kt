package cc.meteormc.hmapro.manager.ui.component.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.meteormc.hmapro.manager.ui.component.EmptyContent
import cc.meteormc.hmapro.manager.ui.component.button.DialogButtonPair
import cc.meteormc.hmapro.manager.ui.util.verticalScrollEffect
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.squircle.squircleClip

@Composable
fun <T> ItemsDialog(
    show: Boolean,
    title: String,
    items: List<T>,
    selected: SnapshotStateSet<T>,
    modifier: Modifier = Modifier,
    transform: @Composable (T) -> String = { it.toString() },
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null
) {
    OverlayDialog(
        show = show,
        title = title,
        largeScreen = true
    ) {
        if (items.isEmpty()) {
            EmptyContent(
                maxHeight = 150.dp
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScrollEffect(
                        enableHaptic = false,
                        enableOverScroll = false
                    )
            ) {
                items(items = items) {
                    CheckboxPreference(
                        title = transform(it),
                        checked = selected.contains(it),
                        onCheckedChange = { checked ->
                            if (checked) {
                                selected += it
                            } else {
                                selected -= it
                            }
                        },
                        modifier = Modifier.squircleClip(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DialogButtonPair(
            leftText = stringResource(android.R.string.cancel),
            rightText = stringResource(android.R.string.ok),
            onLeftClick = { onCancel?.invoke() },
            onRightClick = { onConfirm?.invoke() }
        )
    }
}