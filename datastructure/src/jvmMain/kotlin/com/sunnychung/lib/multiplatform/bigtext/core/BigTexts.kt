package com.sunnychung.lib.multiplatform.bigtext.core

fun BigText.Companion.createFromLargeString(initialContent: String) = createFromString(initialContent)

fun BigText.Companion.createFromSmallString(initialContent: String) =
    createFromString(initialContent, chunkSize = 2 * 1024, undoHistoryCapacity = 100) // 2 KB

fun BigText.Companion.createFromTinyString(initialContent: String) =
    createFromString(initialContent, chunkSize = 32, undoHistoryCapacity = 10) // 32 B

fun BigText.Companion.createFromString(
    initialContent: String,
    chunkSize: Int = 2 * 1024 * 1024,
    undoHistoryCapacity: Int = 1000,
    parallelism: Int = 30,
) =
    BigTextImpl(chunkSize = chunkSize, undoHistoryCapacity = undoHistoryCapacity, parallelism = parallelism).apply {
        log.d { "createFromLargeString ${initialContent.length}" }
        append(initialContent)
        isUndoEnabled = true
    }
