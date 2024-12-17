package com.sunnychung.lib.multiplatform.bigtext.util

fun Char.isSurrogatePairFirst(): Boolean {
    return code in (0xD800 .. 0xDBFF)
}

fun Char.isSurrogatePairSecond(): Boolean {
    return code in (0xDC00 .. 0xDFFF)
}

// copied from Kotlite
fun hexToUtf8String(code: Int): String {
    // convert to bytes according to https://en.wikipedia.org/wiki/UTF-8#Encoding
    val bytes = when {
        code <= 0x007F -> byteArrayOf(code.toByte())
        code <= 0x07FF -> byteArrayOf(
            ((code shr 6) or 0b110_00000).toByte(),
            ((code and 0b00_111111) or 0b10_000000).toByte()
        )
        code <= 0xFFFF -> byteArrayOf(
            ((code shr 12) or 0b1110_0000).toByte(),
            (((code shr 6) and 0b111111) or 0b10_000000).toByte(),
            ((code and 0b111111) or 0b10_000000).toByte(),
        )
        code <= 0x10FFFF -> byteArrayOf(
            ((code shr 18) or 0b11110_000).toByte(),
            (((code shr 12) and 0b111111) or 0b10_000000).toByte(),
            (((code shr 6) and 0b111111) or 0b10_000000).toByte(),
            ((code and 0b111111) or 0b10_000000).toByte(),
        )
        else -> throw RuntimeException("Unsupported unicode character code $code")
    }
    return bytes.decodeToString()
}
