package com.sunnychung.lib.multiplatform.bigtext.util

class StringBuilder2(capacity: Int, private val delegate: StringBuilder = StringBuilder(capacity)) : Appendable by delegate, GeneralStringBuilder {

    override fun clear() {
        delegate.clear()
    }

    override fun toString(): String {
        return delegate.toString()
    }
}
