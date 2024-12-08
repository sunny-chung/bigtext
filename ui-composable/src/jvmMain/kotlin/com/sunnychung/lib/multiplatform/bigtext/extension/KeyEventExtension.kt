package com.sunnychung.lib.multiplatform.bigtext.extension

import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.utf16CodePoint
import com.sunnychung.lib.multiplatform.bigtext.platform.MacOS
import com.sunnychung.lib.multiplatform.bigtext.platform.currentOS

fun KeyEvent.isCtrlOrCmdPressed(): Boolean {
    return if (currentOS() == MacOS) {
        isMetaPressed
    } else {
        isCtrlPressed
    }
}

fun KeyEvent.toTextInput(): String? {
    if (!isTypedEvent) return null
    return StringBuilder().appendCodePoint(utf16CodePoint).toString()
}
