package com.sunnychung.lib.multiplatform.bigtext.demo

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEventType
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation

class PasswordIncrementalTransformation : IncrementalTextTransformation<Unit> {
    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        if (text.isNotEmpty) {
            val length = text.length
            transformer.replace(0 ..< length, "•".repeat(length), BigTextTransformOffsetMapping.Incremental)
        }
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        if (change.eventType == BigTextChangeEventType.Insert) {
            val length = change.changeEndExclusiveIndex - change.changeStartIndex
            transformer.replace(
                range = change.changeStartIndex ..< change.changeEndExclusiveIndex,
                text = "•".repeat(length),
                offsetMapping = BigTextTransformOffsetMapping.Incremental
            )
        }
    }
}
