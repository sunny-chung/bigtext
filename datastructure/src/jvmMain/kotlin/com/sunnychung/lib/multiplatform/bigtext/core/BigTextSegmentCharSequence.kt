package com.sunnychung.lib.multiplatform.bigtext.core

internal class BigTextSegmentCharSequence(
    private val segments: List<BigTextSegment>,
) : CharSequence {
    private val segmentEndOffsets: IntArray

    override val length: Int

    init {
        var accumulatedLength = 0
        segmentEndOffsets = IntArray(segments.size)
        segments.forEachIndexed { index, segment ->
            accumulatedLength += segment.length
            segmentEndOffsets[index] = accumulatedLength
        }
        length = accumulatedLength
    }

    override fun get(index: Int): Char {
        if (index !in 0 ..< length) {
            throw IndexOutOfBoundsException("index=$index, length=$length")
        }
        val segmentIndex = segmentEndOffsets.binarySearch(index + 1).let {
            if (it >= 0) it else -it - 1
        }
        val segmentStartOffset = if (segmentIndex == 0) 0 else segmentEndOffsets[segmentIndex - 1]
        return segments[segmentIndex][index - segmentStartOffset]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        require(startIndex <= endIndex) { "startIndex should be <= endIndex" }
        require(startIndex >= 0) { "startIndex should be >= 0" }
        require(endIndex <= length) { "endIndex should be <= length" }
        if (startIndex == endIndex) {
            return ""
        }

        val result = mutableListOf<BigTextSegment>()
        var remainingStart = startIndex
        val remainingEnd = endIndex
        for (segmentIndex in segments.indices) {
            val segmentStartOffset = if (segmentIndex == 0) 0 else segmentEndOffsets[segmentIndex - 1]
            val segmentEndOffset = segmentEndOffsets[segmentIndex]
            if (remainingEnd <= segmentStartOffset) {
                break
            }
            if (remainingStart >= segmentEndOffset) {
                continue
            }

            val segment = segments[segmentIndex]
            result += segment.slice(
                maxOf(0, remainingStart - segmentStartOffset),
                minOf(segment.length, remainingEnd - segmentStartOffset)
            )
            remainingStart = maxOf(remainingStart, segmentEndOffset)
        }
        return BigTextSegmentCharSequence(result)
    }

    override fun toString(): String = buildString(length) {
        segments.forEach {
            append(it.buffer.subSequence(it.start, it.endExclusive))
        }
    }
}
