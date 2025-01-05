package com.sunnychung.lib.multiplatform.bigtext.core

fun BigText.Companion.createFromLargeString(initialContent: String) = BigTextImpl().apply {
    log.d { "createFromLargeString ${initialContent.length}" }
    append(initialContent)
    isUndoEnabled = true
}
