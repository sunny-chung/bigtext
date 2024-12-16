package com.sunnychung.lib.multiplatform.bigtext.demo

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformer
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation

/**
 * Transform an input so that it is displayed in a format of "(xxx) xxxx-xxxx"
 */
class PhoneNumberIncrementalTransformation : IncrementalTextTransformation<Unit> {
    override fun initialize(text: BigText, transformer: BigTextTransformer) {
        transform(text, transformer)
    }

    override fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: Unit) {
        transform(change.bigText, transformer)
    }

    private fun transform(text: BigText, transformer: BigTextTransformer) {
        log.d { "transform $text ${text.length} '${text.buildString()}'" }
        transformer.restoreToOriginal(0..text.length)
        if (text.isNotEmpty) {
            transformer.insertAt(0, "(")
            if (text.length >= 3) transformer.insertAt(3, ") ")
            if (text.length >= 3 + 4) transformer.insertAt(7, "-")
        }
    }
}
