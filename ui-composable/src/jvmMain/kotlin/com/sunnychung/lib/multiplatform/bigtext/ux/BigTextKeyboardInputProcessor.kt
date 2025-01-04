package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.ui.input.key.KeyEvent

interface BigTextKeyboardInputProcessor {

    fun beforeProcessInput(keyEvent: KeyEvent, viewState: BigTextViewState): Boolean = false

    fun afterProcessInput(keyEvent: KeyEvent, viewState: BigTextViewState): Boolean = false
}
