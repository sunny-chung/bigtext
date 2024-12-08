package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformOffsetMapping

interface LengthNodeValue {
    val leftStringLength: Int

    val bufferLength: Int

    val leftOverallLength: Int
    val currentOverallLength: Int

    val leftRenderLength: Int
    val currentRenderLength: Int

    val transformOffsetMapping: BigTextTransformOffsetMapping
}
