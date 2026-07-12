package com.sunnychung.lib.multiplatform.bigtext.test

import com.sunnychung.lib.multiplatform.bigtext.util.GraphemeClusters
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphemeClustersTest {
    @Test
    fun iteratesEmojiSequencesAsOneCluster() {
        val text = "A👆🏿B👨‍👩‍👧‍👦C🇭🇰D1️⃣Eá"

        val clusters = mutableListOf<String>()
        GraphemeClusters.forEach(text) { start, endExclusive ->
            clusters += text.substring(start, endExclusive)
        }

        assertEquals(
            listOf("A", "👆🏿", "B", "👨‍👩‍👧‍👦", "C", "🇭🇰", "D", "1️⃣", "E", "á"),
            clusters,
        )
    }

    @Test
    fun snapsIndexesToGraphemeBoundaries() {
        val text = "A👆🏿B"

        assertEquals(1, GraphemeClusters.boundaryAtOrBefore(text, 2))
        assertEquals(5, GraphemeClusters.boundaryAtOrAfter(text, 2))
        assertEquals(1, GraphemeClusters.boundaryAtOrBefore(text, 4))
        assertEquals(5, GraphemeClusters.boundaryAtOrAfter(text, 4))
    }
}
