package com.sunnychung.lib.multiplatform.bigtext.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(title = "BigText Demo", onCloseRequest = ::exitApplication) {
            AppView()
        }
    }
}