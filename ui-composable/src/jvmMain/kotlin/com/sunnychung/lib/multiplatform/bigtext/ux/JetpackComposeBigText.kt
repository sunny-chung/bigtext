package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.ui.text.AnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.compose.AnnotatedStringTextBuffer
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.util.AnnotatedStringBuilder

fun BigText.Companion.createFromLargeAnnotatedString(initialContent: AnnotatedString) = BigTextImpl(
    textBufferFactory = { AnnotatedStringTextBuffer(it) },
//    charSequenceBuilderFactory = { AnnotatedString.Builder(it) },
//    charSequenceFactory = { (it as AnnotatedString.Builder).toAnnotatedString() },
    charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
    charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
).apply {
    log.w { "createFromLargeAnnotatedString ${initialContent.length}" }
    append(initialContent)
    isUndoEnabled = true // it has to be after append to avoid recording into the undo history
}
