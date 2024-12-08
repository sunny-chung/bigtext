package com.sunnychung.lib.multiplatform.bigtext.core

abstract class CacheableBigTextDecorator : BigTextDecorator {
    protected var hasInitialized = false

    final override fun initialize(text: BigText) {
        if (hasInitialized) {
            return
        }
        doInitialize(text)
        hasInitialized = true
    }

    open fun doInitialize(text: BigText) = Unit
}
