package com.sunnychung.lib.multiplatform.bigtext.core

data class BigTextChangeEvent(
    /**
     * Unique value to represent the BigText value. This is NOT a sequence number.
     */
    val changeId: Long,

    val bigText: BigText,

    val eventType: BigTextChangeEventType,

    val changeStartIndex: Int,
    val changeEndExclusiveIndex: Int,
)

enum class BigTextChangeEventType {
    Insert, Delete
}
