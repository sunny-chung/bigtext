package com.sunnychung.lib.multiplatform.bigtext.util

/**
 * NOT thread-safe.
 *
 * The purpose of this class is to reduce memory usage by 4 times comparing to List<Int>.
 *
 * An java.lang.Integer in List<Int> consumes 16 bytes, while int consumes 4 bytes.
 */
class IntList(initialCapacity: Int = 16) {

    private var buffer = IntArray(initialCapacity)

    var size: Int = 0
        private set

    val indices: IntRange
        get() = 0 ..< size

    val lastIndex: Int
        get() = size - 1

    operator fun get(index: Int): Int = buffer[index]

    operator fun set(index: Int, value: Int) {
        checkIndex(index)
        buffer[index] = value
    }

    private fun checkIndex(index: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, size: $size")
        }
    }

    private fun growIfNeeded(desiredSize: Int) {
        if (desiredSize > buffer.size) {
            val newSize = 1.shl(32 - Integer.numberOfLeadingZeros(desiredSize)) // increase by 2x
            val newBuffer = IntArray(newSize)
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
            buffer = newBuffer
        }
    }

    fun add(value: Int) {
        growIfNeeded(size + 1)
        buffer[size++] = value
    }

    fun removeLast() {
        --size
    }

    fun clear() {
        size = 0
    }

    operator fun plusAssign(value: Int) {
        add(value)
    }

    override fun toString(): String {
        return buffer.contentToString()
    }
}
