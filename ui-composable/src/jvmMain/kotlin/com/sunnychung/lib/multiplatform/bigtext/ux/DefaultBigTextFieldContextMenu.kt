package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun DefaultBigTextFieldContextMenu(isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String) {
    CursorDropdownMenu(
        expanded = isVisible,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(testTag)
    ) {
        entries.forEach {
            when (it.type) {
                ContextMenuItemEntry.Type.Divider -> Divider()
                ContextMenuItemEntry.Type.Button ->
                    DropdownMenuItem(onClick = {
                        onDismiss()
                        it.action()
                    }, modifier = Modifier.testTag(it.testTag)) {
                        Text(it.displayText)
                    }
            }
        }
    }
}
