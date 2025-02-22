package com.sunnychung.lib.multiplatform.bigtext.util

import java.lang.ref.WeakReference

class WeakRefKey<T : Any>(value: T?) {
    private val ref = WeakReference<T>(value)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val thisValue = ref.get() ?: return false
        if (other is WeakRefKey<*>) {
            val otherValue = other.ref.get() ?: return false
            return thisValue == otherValue
        }

        return thisValue == other
    }

    override fun hashCode(): Int {
        val thisValue = ref.get() ?: return -1
        return thisValue.hashCode()
    }

    fun get(): T? = ref.get()
}

fun <T : Any> weakRefOf(value: T?) = WeakRefKey(value)
