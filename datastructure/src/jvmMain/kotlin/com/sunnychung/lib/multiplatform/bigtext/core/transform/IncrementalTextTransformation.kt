package com.sunnychung.lib.multiplatform.bigtext.core.transform

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent

interface IncrementalTextTransformation<C> {

    fun initialize(text: BigText, transformer: BigTextTransformer): C

    fun beforeTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: C) = Unit
    fun afterTextChange(change: BigTextChangeEvent, transformer: BigTextTransformer, context: C) = Unit

    fun onReapplyTransform(text: BigText, originalRange: IntRange, transformer: BigTextTransformer, context: C) = Unit
}
