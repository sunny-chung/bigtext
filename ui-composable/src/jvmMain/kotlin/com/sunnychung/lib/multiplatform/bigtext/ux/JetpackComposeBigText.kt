package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.ui.text.AnnotatedString
import com.sunnychung.lib.multiplatform.bigtext.compose.AnnotatedStringTextBuffer
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.util.AnnotatedStringBuilder

fun BigText.Companion.createFromLargeAnnotatedString(initialContent: AnnotatedString) =
    createFromAnnotatedString(initialContent)

fun BigText.Companion.createFromSmallAnnotatedString(initialContent: AnnotatedString) =
    createFromAnnotatedString(initialContent, chunkSize = 2 * 1024, undoHistoryCapacity = 100) // 2 KB

fun BigText.Companion.createFromTinyAnnotatedString(initialContent: AnnotatedString) =
    createFromAnnotatedString(initialContent, chunkSize = 32, undoHistoryCapacity = 10) // 32 B

fun BigText.Companion.createFromAnnotatedString(
    initialContent: AnnotatedString,
    chunkSize: Int = 2 * 1024 * 1024,
    undoHistoryCapacity: Int = 1000,
    parallelism: Int = 30,
) = BigTextImpl(
    chunkSize = chunkSize,
    undoHistoryCapacity = undoHistoryCapacity,
    parallelism = parallelism,
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
