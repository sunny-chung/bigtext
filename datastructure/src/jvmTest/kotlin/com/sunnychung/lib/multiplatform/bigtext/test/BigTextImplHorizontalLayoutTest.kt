package com.sunnychung.lib.multiplatform.bigtext.test

import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.core.isD
import com.sunnychung.lib.multiplatform.bigtext.core.layout.MonospaceTextLayouter
import com.sunnychung.lib.multiplatform.bigtext.core.layout.TextLayouter
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformerImpl
import com.sunnychung.lib.multiplatform.bigtext.extension.length
import com.sunnychung.lib.multiplatform.bigtext.test.util.FixedWidthCharMeasurer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@Execution(ExecutionMode.CONCURRENT)
class BigTextImplHorizontalLayoutTest {

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64, 16, 4])
    fun oneLine(chunkSize: Int) {
        listOf(
            "abc de",
            "1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her<e.",
            "1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her<e.ABC",
            "A",
            "",
        ).forEachIndexed { index, testString ->
            val t = BigTextImpl(chunkSize = chunkSize).apply {
                append(testString)
                setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
                setSoftWrapEnabled(false)
            }

            verifyMaxLineWidth(testString, t, message = "test case #$index")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64, 16, 6, 3])
    fun multipleLines(chunkSize: Int) {
        listOf(
            "abcd\nefg",
            "abcd\nefg\n",
            "abc\ndefg",
            "ab\ncdefg\nhij",
            "ab\ncde\nfghij",
            "ab\ncde\nfghij\n",
            "abcd\nef\nghi\n",
            "\nabcd\nef\nghi\n",
            "abcd\nefgh\nijkl\n",
            "\n",
            "\n\n",
            "\n\n\n\n\n",
            "1234567890\n<234567890\n<bcdefghij\n<BCDEFGHIJ\n<row break\n< should h\n<appen her\n<e.",
            "1234567890<234567890\n<bcdefghij<BCDEFGHIJ<row break\n< should h<appen her<e.ABC",
            "1234567890<234567890\n<bcdefghij\n<BCDEFGHIJ\n<row break\n< should h<appen her<e.ABC",
            "1234567890<234567890<bcdefghij\n<BCDEFGHIJ\n<row break\n< should h<appen her<e.ABC",
            "1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her\n<e.ABC",
            "1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her<e.ABC\n",
            "\n1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her<e.ABC",
            "\n1234567890<234567890<bcdefghij<BCDEFGHIJ<row break< should h<appen her<e.ABC\n",
        ).forEachIndexed { index, testString ->
            val t = BigTextImpl(chunkSize = chunkSize).apply {
                append(testString)
                setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
                setSoftWrapEnabled(false)
            }

            if (chunkSize == 3 && index == 12) {
                isD = true
            }

            verifyMaxLineWidth(testString, t, message = "test case #$index")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64 * 1024, 64])
    fun someLongLines(chunkSize: Int) {
        val testString = listOf(29, 1, 0, 133, 5252525, 6009, 0, 0, 100, 1888888, 21, 345, 90002, 3)
            .joinToString("\n") { length -> randomString(length, isAddNewLine = false) }

        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append(testString)
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }

        verifyMaxLineWidth(testString, t)
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64, 16, 6, 3])
    fun inserts(chunkSize: Int) {
        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append("abc\ndefg\n\nhi\njkl")
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        val v = BigTextVerifyImpl(t)
        v.verifyMaxLineWidth("initial")
        listOf(
            t.length to "mn",
            5 to "AB",
            13 to "C",
            13 to "DEFGHIJ",
            t.length + 12 to "K\nXYXY\n",
            0 to "AAA\n",
            0 to "abcde\nfg\nhi\n123456789012345678\njkjk",
            0 to "123456789012345678",
        ).forEachIndexed { index, it ->
            v.insertAt(it.first, it.second)
            v.verifyMaxLineWidth("test case #$index")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64, 16, 6, 3])
    fun deletes(chunkSize: Int) {
        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append("12345678901234567890abc\ndefg1234567890\n\n\n\n1234567890hi\njkl1234567890")
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        val v = BigTextVerifyImpl(t)
        v.verifyMaxLineWidth("initial")
        listOf(
            1 to 3,
            0 to 3,
            0 to 2,
            0 to 12,
            6 to 10,
        ).forEachIndexed { index, it ->
            v.delete(it.first, it.first + it.second)
            v.verifyMaxLineWidth("test case #A$index")
        }
        listOf(
            v.length - 10 to 10,
            12 to 10,
            4 to 9,
        ).forEachIndexed { index, it ->
            v.delete(it.first, it.first + it.second)
            v.verifyMaxLineWidth("test case #B$index")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 64, 16, 6, 3])
    fun replaces(chunkSize: Int) {
        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append("ab\nc")
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        val v = BigTextVerifyImpl(t)
        v.verifyMaxLineWidth("initial")

        listOf(
            (0 ..< t.length) to "12345678901234567890abc\ndefg1234567890\n\n\n\n1234567890hi\njkl1234567890",
            (52 ..< 62) to "A",
            (52 .. 52) to "1234567890123456789012345678901234567890",
            (52 ..< 92) to "1234567890",
            (0 .. 4) to "ABCDE",
            (0 .. 0) to "",
            (0 .. 2) to "",
            (10 ..< 16) to "-",
            (19 ..< 29) to "F",
            (0 .. 10) to "G",
        ).forEachIndexed { index, it ->
            v.replace(it.first, it.second)
            v.verifyMaxLineWidth("test case #A$index")
        }
        listOf(
            2 to "w",
            1 to "x",
            2 to "y",
            2 to "z",
        ).forEachIndexed { index, it ->
            v.replace(t.length - it.first ..< t.length, it.second)
            v.verifyMaxLineWidth("test case #B$index")
        }
        listOf(
            (14 ..< 34) to "==",
            (14 ..< 17) to "",
            (6 .. 8) to "+",
            (2 .. 6) to "~",
            (4 .. 7) to "H",
            (1 .. 6) to "I",
            (0 .. 1) to "",
        ).forEachIndexed { index, it ->
            v.replace(it.first, it.second)
            v.verifyMaxLineWidth("test case #C$index")
        }
        println(v.buildString())
    }

    @ParameterizedTest
    @ValueSource(ints = [256, 65536, 1 * 1024 * 1024])
    @Order(Integer.MAX_VALUE - 100) // This test is pretty time-consuming. Run at the last!
    fun manyInserts(chunkSize: Int) {
        random = Random(10234567) // use a fixed seed for easier debug
        repeat(10) { repeatIt ->
            val initial = randomString(if (repeatIt < 3) 0 else random.nextInt(10000), isAddNewLine = true)
            val t = BigTextVerifyImpl(chunkSize = chunkSize).apply {
                append(initial)
                bigTextImpl.setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
                setSoftWrapEnabled(false)
            }
            val numInsertTimes = 1007
            repeat(numInsertTimes) { i ->
                val length = when (random.nextInt(100)) {
                    in 0..44 -> random.nextInt(10)
                    in 45..69 -> random.nextInt(10, 1000)
                    in 70..87 -> random.nextInt(1000, 10000)
                    in 88..97 -> random.nextInt(10000, 700_000)
                    in 98..99 -> random.nextInt(700_000, 2_500_000)
                    else -> throw IllegalStateException()
                }
                val pos = when (random.nextInt(10)) {
                    in 0..1 -> 0
                    in 2..3 -> t.length
                    else -> random.nextInt(t.length + 1)
                }
                t.insertAt(pos, randomString(length, isAddNewLine = random.nextBoolean()))
                t.verifyMaxLineWidth("it $repeatIt, $i")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [256, 65536, 1 * 1024 * 1024])
    @Order(Integer.MAX_VALUE - 100) // This test is pretty time-consuming. Run at the last!
    fun manyDeletes(chunkSize: Int) {
        random = Random(20345678) // use a fixed seed for easier debug
        repeat(10) { repeatIt ->
            val initial = randomString(random.nextInt(35_700_000, 39_600_000), isAddNewLine = true)
            val t = BigTextVerifyImpl(chunkSize = chunkSize).apply {
                append(initial)
                bigTextImpl.setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
                bigTextImpl.setSoftWrapEnabled(false)
            }
            val numDeleteTimes = 1007
            repeat(numDeleteTimes) { i ->
                val length = when (random.nextInt(100)) {
                    in 0..44 -> random.nextInt(10)
                    in 45..59 -> random.nextInt(10, 100)
                    in 60..77 -> random.nextInt(100, 1000)
                    in 78..87 -> random.nextInt(1000, 10000)
                    in 88..96 -> random.nextInt(10000, 200_000)
                    in 97..98 -> random.nextInt(200_000, 750_000)
                    99 -> random.nextInt(750_000, 2_300_000)
                    else -> throw IllegalStateException()
                }
                val pos = when (random.nextInt(10)) {
                    in 0..1 -> 0
                    in 2..3 -> maxOf(0, t.length - length)
                    else -> random.nextInt(t.length + 1)
                }
                t.delete(pos, minOf(t.length, pos + length))
                t.verifyMaxLineWidth("it $repeatIt, $i")
            }
        }
    }

    @Disabled
    @Deprecated("Use findWidthByPositionRangeOfSameLine instead.")
    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 32, 16, 6, 3])
    fun findWidthByColumnRangeOfSameLine(chunkSize: Int) {
        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append("1234\n1234567890123456\n12345678901234567890123456789\n\n")
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(16f)))
            setSoftWrapEnabled(false)
        }
        listOf(
            0 to (0 .. 2),
            0 to (0 .. 3),
            0 to (0 .. 0),
            0 to (1 .. 1),
            0 to (2 .. 2),
            0 to (3 .. 3),
            1 to (0 .. 9),
            1 to (0 .. 10),
            1 to (0 .. 14),
            1 to (0 .. 15),
            2 to (0 .. 26),
            2 to (0 ..< 29),
            2 to (5 ..< 29),
            2 to (17 ..< 29),
            2 to (28 ..< 29),
            3 to (0 .. 0),
        ).forEachIndexed { index, (lineIndex, columns) ->
            assertEquals(
                columns.length * 16f,
                t.findWidthByColumnRangeOfSameLine(lineIndex, columns),
                0.01f,
                "test case #$index"
            )
        }

        // end of string, width = 0
        assertEquals(
            0f,
            t.findWidthByColumnRangeOfSameLine(4, 0 .. 0),
            0.01f,
            "end"
        )
    }

    @Test
    fun findWidthSum() {
        val random = Random(234)
        val charWidths = (0 .. 9).map { random.nextInt(1000) + 1 }
        val layouter = object : TextLayouter {
            override fun indexCharWidth(text: String) = throw NotImplementedError()

            override fun measureCharWidth(char: String): Float {
                if (char == "\n") return 0f
                return charWidths[(char[0] - '0')].toFloat()
            }

            override fun measureCharYOffset(char: String): Float = throw NotImplementedError()

            override fun layoutOneLine(
                line: CharSequence,
                contentWidth: Float,
                firstRowOccupiedWidth: Float,
                offset: Int
            ): Pair<List<Int>, Float> = throw NotImplementedError()

        }

        val t = BigTextImpl(chunkSize = 1024).apply {
            append("1234\n1234567890123456\n12345678901234567890123456789\n\n26378942103594682936\n489\n\n\n")
            setLayouter(layouter)
            setSoftWrapEnabled(false)
        }
        val buffer = t.buffers.first()
        val bufferExtraData = t.bufferExtraData[buffer]!!

        fun verifyWidths(position: Int) {
            val measured = t.findWidthSum(buffer, bufferExtraData, position)
            val expected = (0 .. position).sumOf {
                (layouter.measureCharWidth(t.substring(it, it + 1).toString()) * t.widthMultiplier).toLong()
            }
            println("[$position] exp=$expected, mes=$measured")
            assertEquals(expected, measured, "pos $position")
        }

        (-1 .. t.lastIndex).forEach {
            verifyWidths(it)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1 * 1024 * 1024, 32])
    fun findWidthByPositionRangeOfSameLine(chunkSize: Int) {
        listOf(
            "1234\n1234567890123456\n12345678901234567890123456789\n\n26378942103594682936\n5\n1029384756019283457657412895071228537042987590385479290819257214510926782450428902864592150125913597\n489\n\n\n",
            "145274309673245\n1\n25"
        ).forEach { testCase ->
            val random = Random(2345)
            val charWidths = (0..9).map { random.nextInt(1000) + 1 }
            val layouter = object : TextLayouter {
                override fun indexCharWidth(text: String) = throw NotImplementedError()

                override fun measureCharWidth(char: String): Float {
                    if (char == "\n") return 0f
                    return charWidths[(char[0] - '0')].toFloat()
                }

                override fun measureCharYOffset(char: String): Float = throw NotImplementedError()

                override fun layoutOneLine(
                    line: CharSequence,
                    contentWidth: Float,
                    firstRowOccupiedWidth: Float,
                    offset: Int
                ): Pair<List<Int>, Float> = throw NotImplementedError()

            }

            val t = BigTextImpl(chunkSize = chunkSize).apply {
                append(testCase)
                setLayouter(layouter)
                setSoftWrapEnabled(false)
            }

            fun verifyWidths(positions: IntRange) {
                val measured = t.findWidthByPositionRangeOfSameLine(positions).toDouble()
                val expected = positions.sumOf {
                    layouter.measureCharWidth(t.substring(it, it + 1).toString()).toDouble()
                }
                println("[$positions] exp=$expected, mes=$measured")
                assertEquals(expected, measured, 0.0001, "pos $positions")
            }

            fun findLineIndex(position: Int): Int {
                if (position <= 0) return 0
                return t.substring(0..<position).count { it == '\n' }
            }

            (0..t.lastIndex).forEach { i ->
                val lineI = findLineIndex(i)
                (i - 1..t.lastIndex).forEach { j ->
                    val lineJ = findLineIndex(j)
                    if (lineI == lineJ) {
                        verifyWidths(i..j)
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2 * 1024 * 1024, 1024, 256, 32])
    fun maxLineWidthOfTransformed(chunkSize: Int) {
//    @Test
//    fun maxLineWidthOfTransformed() {
//        val chunkSize = 2 * 1024 * 1024
        val testString = """
            9KHzmFawZdNdy e3NGXV O8Al3YdOO L8247gv91nh4DgELJJvej0eqZnVdAdudt4pGZrQSOc2Nqb8PuJrqdP85zt  fRsh8sELKKyKMf334Fo0L F YDZoLlhrj7piBhDa VtNDU8uxscg0 6ds1 k5xXPWZ3ia eHzAbwAb3YF0SH yy 1wcyPdhGUkaUZTuBCiJJOltOZpJhC9PT3y2Lr6bjQYLfjN0HEvSwNtyy4Cyh6obi9GLbg9aC Ec G7eeXiDvaFMcu2PTO7LBmpeY5R kAJMm3BlmEfrJCqeyL353GY6dSJezGmpGkbQF5WVFQRb4DCT47r8CaTn  CNJWMYp zbIKS ntV5o8CkZN6yPZfscv1vJvOp0HZBL8 fSp${'$'}{{Jx3zwcMab1gpTpGWWQh}}n61 oYjsV5 NFQgr${'$'}{{F45fB2DNYxZy2G0W4j}}Xe71FN1iDWVeKlPQ 6w16J8ZnS8ClSNiVDDL68gbUETUVDFwYY5quU3Rb OpnBA bI3ubqVI0nQ7gxZUcLJgST41Ztj9CpDBEwoDPzAD8
            cicNL
            lkamzW0DIMxzlRAQ2b Ph DIqaUVwklNlNff  Jl12n3PJDO6jqK7b0PyuLIfKE3Xp0C7Z5qDrF5uBVC4YT9v84
            2xhe i y7 FiZcv MjniLD mU8rddDzJJf0 HvwufgFwFXQEKxfLTEnuXG8JZ9fy4SA3UJRpoDQipWFvzirWm nG0UkOp142LXnrH
             eOcmq0 XeUsROg GRjM5QITAR0nkWenNRiIp4P
            g pYmpFS RopRfdr r9M p3JJWH9ml n2lhXYKxk8b3zHBfDFP172HSjSRTBLdkk2
            STgyGIO XrTAOJ
            YOs6uRfWnVfVdP6RXEKVQhyjAk 40Iqiz6 XMO1DD3 cvo283ZySgM9Nmg4Ky 3r84Zr0shtkD

             deGkn  RggXCxTsfWKQio3n1 AAhTFlwV RVhTNO7azfAUA${'$'}{{S}} Z
            uVL3i060yEdMSflrTiInVhON7TArEl N
        """.trimIndent()
        val t = BigTextImpl(chunkSize = chunkSize).apply {
            append(testString)
            setLayouter(MonospaceTextLayouter(FixedWidthCharMeasurer(17.31f)))
            setSoftWrapEnabled(false)
        }
        val tt = BigTextTransformerImpl(t)
        val transformPattern = "\\$\\{\\{([^}]+)\\}\\}".toRegex()
        transformPattern.findAll(testString).forEach {
            val replacement = it.groups[1]!!.value
            tt.replace(it.range, replacement)
        }
        tt.printDebug()
        val testStringTransformed = testString.replace(transformPattern) { it.groups[1]!!.value }
        verifyMaxLineWidth(testStringTransformed, tt, charWidth = 17.31f)
    }

    internal fun BigTextVerifyImpl.verifyMaxLineWidth(message: String? = null) {
        verifyMaxLineWidth(stringImpl.buildString(), bigTextImpl, message = message)
    }

    internal fun verifyMaxLineWidth(testString: String, t: BigTextImpl, charWidth: Float = 16f, message: String? = null) {
        assertEquals(testString.split('\n').maxOf { it.length } * (charWidth * t.widthMultiplier).toLong(), t.maxLineWidth, message)
    }
}
