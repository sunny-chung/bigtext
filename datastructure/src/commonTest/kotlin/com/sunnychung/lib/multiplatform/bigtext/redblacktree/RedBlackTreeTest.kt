package com.sunnychung.lib.multiplatform.bigtext.redblacktree

import kotlin.test.*

class RedBlackTreeTest {

    private fun <K : Comparable<K>, V> assertTreeState(
        tree: RedBlackTree<K, V>,
        expectedInOrder: List<Pair<K, V>>
    ) {
        assertEquals(expectedInOrder, tree.inOrder(), "In-order traversal mismatch")
        assertTrue(tree.isValidRBTree(), "Tree is not a valid Red-Black Tree after operation")
    }

    @Test
    fun testEmptyTree() {
        val tree = RedBlackTree<Int, String>()
        assertNull(tree.get(1))
        assertFalse(tree.contains(1))
        assertEquals(emptyList(), tree.inOrder())
        assertTrue(tree.isValidRBTree())
    }

    @Test
    fun testSingleInsertAndDelete() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(1, "one")
        assertEquals("one", tree.get(1))
        assertTrue(tree.contains(1))
        assertTreeState(tree, listOf(1 to "one"))

        tree.remove(1)
        assertNull(tree.get(1))
        assertFalse(tree.contains(1))
        assertTreeState(tree, emptyList())
    }

    @Test
    fun testDuplicateKeyInsert() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(42, "first")
        tree.insert(42, "second")
        assertEquals("second", tree.get(42))
        assertTreeState(tree, listOf(42 to "second"))
    }

    @Test
    fun testRemoveMinAndMax() {
        val tree = RedBlackTree<Int, String>()
        for (n in 1..5) tree.insert(n, "v$n")
        tree.remove(1) // min
        tree.remove(5) // max
        assertTreeState(tree, listOf(2 to "v2", 3 to "v3", 4 to "v4"))
    }

    @Test
    fun testRemoveNonExistent() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(100, "hundred")
        tree.remove(999) // should not affect tree
        assertEquals("hundred", tree.get(100))
        assertTreeState(tree, listOf(100 to "hundred"))
    }

    @Test
    fun testRemoveRootNode() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(10, "root")
        tree.insert(5, "left")
        tree.insert(15, "right")
        tree.remove(10)
        assertNull(tree.get(10))
        assertTreeState(tree, listOf(5 to "left", 15 to "right"))
    }

    @Test
    fun testInOrderAfterRandomInserts() {
        val tree = RedBlackTree<Int, String>()
        val keys = listOf(4, 2, 5, 1, 3)
        for (k in keys) tree.insert(k, "v$k")
        val expected = keys.sorted().map { it to "v$it" }
        assertTreeState(tree, expected)
    }

    @Test
    fun testEdgeCaseAllLeft() {
        val tree = RedBlackTree<Int, String>()
        for (n in 10 downTo 1) tree.insert(n, "v$n")
        val expected = (1..10).map { it to "v$it" }
        assertTreeState(tree, expected)
    }

    @Test
    fun testEdgeCaseAllRight() {
        val tree = RedBlackTree<Int, String>()
        for (n in 1..10) tree.insert(n, "v$n")
        val expected = (1..10).map { it to "v$it" }
        assertTreeState(tree, expected)
    }

    @Test
    fun testInsertAndRemoveAlternating() {
        val tree = RedBlackTree<Int, String>()
        val expected = mutableListOf<Pair<Int, String>>()
        for (i in 1..5) {
            tree.insert(i, "v$i")
            expected.add(i to "v$i")
            assertTreeState(tree, expected.sortedBy { it.first })
        }
        for (i in 1..5) {
            tree.remove(i)
            expected.removeAll { it.first == i }
            assertTreeState(tree, expected.sortedBy { it.first })
        }
    }

    @Test
    fun testManyInsertionsAndDeletions() {
        val tree = RedBlackTree<Int, Int>()
        val numbers = (1..100).shuffled()
        numbers.forEach { tree.insert(it, it * 10) }
        val expected = (1..100).map { it to it * 10 }
        assertTreeState(tree, expected.sortedBy { it.first })

        (1..50).forEach { tree.remove(it) }
        val expected2 = (51..100).map { it to it * 10 }
        assertTreeState(tree, expected2.sortedBy { it.first })
    }

    @Test
    fun testManyInsertionsAndDeletions2() {
        val tree = RedBlackTree<Int, Int>()
        val numbers = (1..100).shuffled()
        for (n in numbers) tree.insert(n, n * 10)
        for (n in 1..100) assertEquals(n * 10, tree.get(n))
        for (n in 1..50) tree.remove(n)
        for (n in 1..50) assertNull(tree.get(n))
        for (n in 51..100) assertEquals(n * 10, tree.get(n))
        assertTrue(tree.isValidRBTree())
    }

    @Test
    fun testRemoveNodeWithOneChild() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(10, "a")
        tree.insert(5, "b")
        tree.insert(3, "c")
        tree.remove(5)
        assertNull(tree.get(5))
        assertEquals("c", tree.get(3))
        assertTreeState(tree, listOf(3 to "c", 10 to "a"))
    }

    @Test
    fun testRemoveNodeWithTwoChildren() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(10, "a")
        tree.insert(5, "b")
        tree.insert(15, "c")
        tree.insert(13, "d")
        tree.insert(17, "e")
        tree.remove(15)
        assertNull(tree.get(15))
        assertEquals("d", tree.get(13))
        assertEquals("e", tree.get(17))
        val expected = listOf(5 to "b", 10 to "a", 13 to "d", 17 to "e")
        assertTreeState(tree, expected)
    }

    @Test
    fun testRemoveLeaf() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(10, "a")
        tree.insert(5, "b")
        tree.insert(15, "c")
        tree.remove(5)
        assertNull(tree.get(5))
        val expected = listOf(10 to "a", 15 to "c")
        assertTreeState(tree, expected)
    }

    @Test
    fun testInsertAndSearch() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(10, "a")
        assertEquals("a", tree.get(10))
        assertNull(tree.get(5))
        tree.insert(5, "b")
        tree.insert(15, "c")
        assertEquals("b", tree.get(5))
        assertEquals("c", tree.get(15))
    }

    @Test
    fun testOverwriteValue() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(1, "a")
        tree.insert(1, "b")
        assertTreeState(tree, listOf(1 to "b"))
        assertEquals("b", tree.get(1))
    }

    @Test
    fun testContains() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(1, "x")
        assertTrue(tree.contains(1))
        assertFalse(tree.contains(2))
        assertTreeState(tree, listOf(1 to "x"))
    }

    @Test
    fun testInOrder() {
        val tree = RedBlackTree<Int, String>()
        tree.insert(3, "c")
        tree.insert(1, "a")
        tree.insert(2, "b")
        val expected = listOf(1 to "a", 2 to "b", 3 to "c")
        assertTreeState(tree, expected)
    }

    @Test
    fun testGetOnEmptyAndAfterDeletes() {
        val tree = RedBlackTree<Int, String>()
        assertNull(tree.get(100))
        tree.insert(100, "x")
        assertEquals("x", tree.get(100))
        tree.remove(100)
        assertNull(tree.get(100))
        assertTreeState(tree, emptyList())
    }

    @Test
    fun testRBPropertiesAfterStress() {
        val tree = RedBlackTree<Int, Int>()
        for (i in 1..200) tree.insert(i, i)
        for (i in 1..100) tree.remove(i)
        for (i in 201..300) tree.insert(i, i)
        for (i in 101..150) tree.remove(i)
        assertTrue(tree.isValidRBTree())
        val expected = (151..300).map { it to it }
        assertTreeState(tree, expected)
    }

    @Test
    fun testMixOf20InsertsAndDeletes() {
        val tree = RedBlackTree<Int, String>()
        val actions = listOf(
            { tree.insert(10, "ten") },
            { tree.insert(20, "twenty") },
            { tree.insert(5, "five") },
            { tree.insert(15, "fifteen") },
            { tree.insert(25, "twenty-five") },
            { tree.remove(10) },
            { tree.insert(7, "seven") },
            { tree.remove(25) },
            { tree.insert(3, "three") },
            { tree.insert(17, "seventeen") },
            { tree.remove(5) },
            { tree.insert(2, "two") },
            { tree.remove(20) },
            { tree.insert(30, "thirty") },
            { tree.insert(1, "one") },
            { tree.remove(3) },
            { tree.insert(40, "forty") },
            { tree.insert(50, "fifty") },
            { tree.remove(15) },
            { tree.insert(60, "sixty") }
        )
        val expectedStates = listOf(
            listOf(10 to "ten"),
            listOf(10 to "ten", 20 to "twenty"),
            listOf(5 to "five", 10 to "ten", 20 to "twenty"),
            listOf(5 to "five", 10 to "ten", 15 to "fifteen", 20 to "twenty"),
            listOf(5 to "five", 10 to "ten", 15 to "fifteen", 20 to "twenty", 25 to "twenty-five"),
            listOf(5 to "five", 15 to "fifteen", 20 to "twenty", 25 to "twenty-five"),
            listOf(5 to "five", 7 to "seven", 15 to "fifteen", 20 to "twenty", 25 to "twenty-five"),
            listOf(5 to "five", 7 to "seven", 15 to "fifteen", 20 to "twenty"),
            listOf(3 to "three", 5 to "five", 7 to "seven", 15 to "fifteen", 20 to "twenty"),
            listOf(3 to "three", 5 to "five", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 20 to "twenty"),
            listOf(3 to "three", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 20 to "twenty"),
            listOf(2 to "two", 3 to "three", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 20 to "twenty"),
            listOf(2 to "two", 3 to "three", 7 to "seven", 15 to "fifteen", 17 to "seventeen"),
            listOf(2 to "two", 3 to "three", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 30 to "thirty"),
            listOf(1 to "one", 2 to "two", 3 to "three", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 30 to "thirty"),
            listOf(1 to "one", 2 to "two", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 30 to "thirty"),
            listOf(1 to "one", 2 to "two", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 30 to "thirty", 40 to "forty"),
            listOf(1 to "one", 2 to "two", 7 to "seven", 15 to "fifteen", 17 to "seventeen", 30 to "thirty", 40 to "forty", 50 to "fifty"),
            listOf(1 to "one", 2 to "two", 7 to "seven", 17 to "seventeen", 30 to "thirty", 40 to "forty", 50 to "fifty"),
            listOf(1 to "one", 2 to "two", 7 to "seven", 17 to "seventeen", 30 to "thirty", 40 to "forty", 50 to "fifty", 60 to "sixty")
        )
        actions.forEachIndexed { i, action ->
            action()
            assertTreeState(tree, expectedStates.getOrElse(i) { expectedStates.last() }.sortedBy { it.first })
        }
    }
}
