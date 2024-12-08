package com.sunnychung.lib.multiplatform.bigtext.core

interface BigTextChangeHook {

    fun afterInsertChunk(modifiedText: BigText, position: Int, newValue: BigTextNodeValue)

    fun afterDelete(modifiedText: BigText, position: IntRange)
}
