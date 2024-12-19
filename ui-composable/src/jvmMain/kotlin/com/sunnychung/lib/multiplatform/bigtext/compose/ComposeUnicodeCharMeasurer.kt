package com.sunnychung.lib.multiplatform.bigtext.compose

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.sunnychung.lib.multiplatform.bigtext.core.layout.CharMeasurer
import com.sunnychung.lib.multiplatform.bigtext.util.isSurrogatePairFirst
import com.sunnychung.lib.multiplatform.bigtext.util.isSurrogatePairSecond
import com.sunnychung.lib.multiplatform.bigtext.util.log
import java.util.concurrent.ConcurrentHashMap

private val charsRequiringWrapping = setOf(" ", "\t")

class ComposeUnicodeCharMeasurer(private val measurer: TextMeasurer, private val style: TextStyle/*, val density: Density, val fontFamilyResolver: FontFamily.Resolver*/) : CharMeasurer {
    private val charWidth: MutableMap<String, Float> = ConcurrentHashMap<String, Float>(128) //LinkedHashMap<String, Float>(256)
    private val charYOffset: MutableMap<String, Float> = ConcurrentHashMap<String, Float>(128) //LinkedHashMap<String, Float>(256)
    private val refCharHeight: Float = measurer.measure("|\n|", style, softWrap = false).let {
        log.w { "charHeight ${it.getLineTop(1)} - ${it.getLineTop(0)}" }
        it.getLineTop(1) - it.getLineTop(0)
    }
    private val refChar = 'A'
    private val refCharWidth: Float
    private val refCharHeightBaselineDiff: Float
    init {
        val it = measurer.measure("$refChar", style, softWrap = false)
        refCharWidth = it.getLineRight(0) - it.getLineLeft(0)
        refCharHeightBaselineDiff = it.getLineBottom(0) - it.firstBaseline
    }
    private val numRepeatMeasurePerChar = 1 // 10

    /**
     * Time complexity = O(S lg C)
     */
    override fun measureFullText(text: String) {
        val charToMeasure = mutableSetOf<String>()
        var surrogatePairFirst: Char? = null
        text.forEach {
            var s = it.toString()
            if (surrogatePairFirst == null && s[0].isSurrogatePairFirst()) {
                surrogatePairFirst = s[0]
                return@forEach
            } else if (surrogatePairFirst != null) {
                if (s[0].isSurrogatePairSecond()) {
                    s = "$surrogatePairFirst${s[0]}"
                } else {
                    s = s.substring(0, 1)
                }
                surrogatePairFirst = null
            }
            if (!charWidth.containsKey(s) && shouldIndexChar(s)) {
                charToMeasure += s
            }
        }
        measureAndIndex(charToMeasure)
        log.v { "UnicodeCharMeasurer measureFullText cache size ${charWidth.size}" }
    }

    /**
     * Time complexity = O(lg C)
     *
     * TODO: handle surrogate pair correctly
     */
    override fun findCharWidth(char: String): Float {
        if (char[0].isSurrogatePairFirst() && char.length == 1) {
            return 0f
        }
        when (char.codePoints().findFirst().asInt) {
            in 0x4E00..0x9FFF,
                in 0x3400..0x4DBF,
                in 0x20000..0x2A6DF,
                in 0xAC00..0xD7AF ->
                    return charWidth[CJK_FULLWIDTH_REPRESENTABLE_CHAR]!!
        }
        return charWidth[char] ?: run {
            measureAndIndex(setOf(char))
            charWidth[char] ?: 0f // it is possible to be not found due to negative widths
        }
    }

    override fun findCharYOffset(char: String): Float {
        if (char[0].isSurrogatePairFirst() && char.length == 1) {
            return 0f //refCharHeight
        }
        return charYOffset[char] ?: run {
            measureAndIndex(setOf(char))
            charYOffset[char]!!
        }
    }

    private fun measureAndIndex(charSet: Set<String>) {
        val chars = charSet.toList()
        measure(chars).forEachIndexed { index, r ->
            if (r.first >= 0f) {
                charWidth[chars[index]] = r.first
            }
            if (r.first < 1f) {
                log.w { "measure '${chars[index]}' width = $r" }
            }
            charYOffset[chars[index]] = r.second
        }
    }

    fun getRowHeight(): Float = refCharHeight

    fun measure(targets: List<String>): List<Pair<Float, Float>> {
        log.d { "measure ${targets.size} targets" }
//        return targets.map { measurer.measure(it, style, softWrap = false).getBoundingBox(0).width }

        // wrapping a target is needed because the measurer would trim strings
        val result = measurer.measure(targets.joinToString("") {
            if (it in charsRequiringWrapping) {
                "$refChar${it.repeat(numRepeatMeasurePerChar)}$refChar\n"
            } else {
                "$it\n"
            }
        }, style, softWrap = false, maxLines = targets.size + 1, overflow = TextOverflow.Visible)
        var charIndex = 0
        return targets.mapIndexed { index, s ->
            val indexOffset: Int
            val w = if (s in charsRequiringWrapping) {
                indexOffset = 1
                ((result.getLineRight(index) - result.getLineLeft(index) - 2 * refCharWidth) / numRepeatMeasurePerChar)
            } else {
                indexOffset = 0
                result.getLineRight(index) - result.getLineLeft(index)
            }
            /*.let {
                val b = measurer.measure(s, style, softWrap = false, maxLines = 1, overflow = TextOverflow.Visible).getBoundingBox(0)
//                val b = measurer.measure(s, style, softWrap = false).size
                val p = Paragraph(s, style, Constraints(), density, fontFamilyResolver).let {
                    it.getLineRight(0) - it.getLineLeft(0)
                }
                if (abs(it - b.width) >= 0.009) {
                    log.w { "different char measurement on '$s': $it VS ${b.width} VS ${measurer.measure(s, style, softWrap = false, maxLines = 1, overflow = TextOverflow.Visible).let { it.getLineRight(0) - it.getLineLeft(0) }} VS $p" }
                }
                b.width
            }*/
            log.w { "char '$s' bl=${result.firstBaseline} r=$result top=${result.getLineTop(index)} bottom=${result.getLineBottom(index)} bound=${result.getBoundingBox(charIndex + indexOffset)} rbound=${result.getBoundingBox(charIndex)}" }
            w to refCharHeight - result.firstBaseline - refCharHeightBaselineDiff /* from observation, `result.getLineTop()` <= 0 */
                .also {
                    charIndex += s.length
                }
        }
    }

    inline fun shouldIndexChar(s: String): Boolean {
        val cp = s.codePoints().findFirst().asInt
        return when (cp) {
            in 0x4E00..0x9FFF -> false // CJK Unified Ideographs
            in 0x3400..0x4DBF -> false // CJK Unified Ideographs Extension A
            in 0x20000..0x2A6DF -> false // CJK Unified Ideographs Extension B
            in 0xAC00..0xD7AF -> false // Hangul Syllables
            else -> true
        }
    }

    init {
        measureAndIndex(COMPULSORY_MEASURES)
        // hardcode, because calling TextMeasurer#measure() against below characters returns zero width
//        charWidth[" "] = charWidth["_"]!!
//        charWidth["\t"] = charWidth[" "]!!
//        charWidth["?"] = charWidth["!"]!!
//        charWidth["’"] = charWidth["'"]!!
        log.d { "$this cache: $charWidth" }
        log.d { "$this space 1=${measure(listOf(" "))} 2=${measure(listOf("  "))} 3=${measure(listOf("   "))}" }
    }

    companion object {
        private const val CJK_FULLWIDTH_REPRESENTABLE_CHAR = "好"
        private val COMPULSORY_MEASURES = (
            (0x20.toChar() .. 0x7E.toChar()).map(Char::toString).toMutableSet() +
                CJK_FULLWIDTH_REPRESENTABLE_CHAR
        ).toSet()
    }
}
