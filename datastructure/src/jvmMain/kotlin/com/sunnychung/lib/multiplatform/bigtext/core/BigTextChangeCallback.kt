package com.sunnychung.lib.multiplatform.bigtext.core

interface BigTextChangeCallback {

    fun onValuePreChange(eventType: BigTextChangeEventType, changeStartIndex: Int, changeEndExclusiveIndex: Int) = Unit

    fun onValuePostChange(eventType: BigTextChangeEventType, changeStartIndex: Int, changeEndExclusiveIndex: Int) = Unit
}
