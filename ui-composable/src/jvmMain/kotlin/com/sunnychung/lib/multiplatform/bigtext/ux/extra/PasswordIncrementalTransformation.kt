package com.sunnychung.lib.multiplatform.bigtext.ux.extra

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEventType
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation

class PasswordIncrementalTransformation(val mask: Char = '\u2022') : IncrementalTextTransformation<Unit> {
    private val maskString = mask.toString()

    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        if (text.isNotEmpty) {
            val length = text.length
            transformer.replace(0 ..< length, maskString.repeat(length), BigTextTransformOffsetMapping.Incremental)
        }
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        if (change.eventType == BigTextChangeEventType.Insert) {
            val length = change.changeEndExclusiveIndex - change.changeStartIndex
            transformer.replace(
                range = change.changeStartIndex ..< change.changeEndExclusiveIndex,
                text = maskString.repeat(length),
                offsetMapping = BigTextTransformOffsetMapping.Incremental
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PasswordIncrementalTransformation) return false

        if (mask != other.mask) return false

        return true
    }

    override fun hashCode(): Int {
        return mask.hashCode() * 31 + 11
    }
}
