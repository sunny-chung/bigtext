package com.sunnychung.lib.multiplatform.bigtext.core

import com.sunnychung.lib.multiplatform.bigtext.redblacktree.Color
import com.sunnychung.lib.multiplatform.bigtext.redblacktree.RedBlackTree

open class LengthTree<V>(private val computations: RedBlackTreeComputations2<V>) : RedBlackTree<@UnsafeVariance V, V>() //(computations)
        where V : LengthNodeValue, V : Comparable<@UnsafeVariance V>, V : DebuggableNode2<in @UnsafeVariance V> {

    // RB Tree Supplement

    override fun insert(key: V, value: V): Node? {
        throw UnsupportedOperationException()
    }

    fun insert(value: V): Node? {
        val z: RedBlackTree<V, V>.Node? = super.insert(value, value)
        z?.let {
            value.attach(it)
        }
        return z
    }

    fun insertBefore(navigator: (Node) -> Int, value: V): Node {
        val key = value
        var y: Node? = null
        var x = root
        var compareResultOfY = 0

        while (x != null) {
            y = x
            compareResultOfY = navigator(x)
            when (compareResultOfY) {
                in -Int.MAX_VALUE .. 0 -> x = x.left
                else /*key > x.key*/ -> x = x.right
            }
        }
        log.d { "insert $value before node y = ${y?.value?.debugKey()}" }

        val z = Node(key, value, Color.RED, null, null, y)
        if (y == null) {
            root = z
        } else if (compareResultOfY <= 0) {
            y.left = z
        } else {
            y.right = z
        }

        insertFixup(z)
        ++size
        value.attach(z)
        computations.recomputeFromLeaf(z) // z has already been computed outside. skipping
//        y?.let { y -> computations.recomputeFromLeaf(y) }
        return z
    }

    fun insertBeforePosition(position: Int, value: V): Node {
        var findBasePosition = 0
        return insertBefore({
            val direction = if (position < findBasePosition + it.value.leftStringLength + it.value.bufferLength) {
                -1
            } else {
                1
            }
            if (direction > 0) {
                findBasePosition += it.value.leftStringLength + it.value.bufferLength
            }
            direction
        }, value)
    }

    override fun rotateLeft(x: RedBlackTree<V, V>.Node): Boolean {
        return super.rotateLeft(x).also {
            if (it) {
                computations.recomputeFromLeaf(x)
            }
        }
    }

    override fun rotateRight(x: RedBlackTree<V, V>.Node): Boolean {
        return super.rotateRight(x).also {
            if (it) {
                computations.recomputeFromLeaf(x)
            }
        }
    }

    override fun transplant(u: RedBlackTree<V, V>.Node, v: RedBlackTree<V, V>.Node?) {
        log.v { "transplant: ${u.value?.debugKey()} > ${v?.value?.debugKey()}" }
        super.transplant(u, v)
        v?.value?.attach(v)
//        v?.let { v -> computations.recomputeFromLeaf(v) }
    }

    override fun recompute(n: RedBlackTree<V, V>.Node) {
        computations.recomputeFromLeaf(n)
    }

    override fun deleteNode(z: RedBlackTree<V, V>.Node) {
        super.deleteNode(z)
        z.value.detach()
    }

    @JvmName("getRoot2")
    fun getRoot(): Node? = root

    @JvmName("setRoot2")
    fun setRoot(z: Node?) {
        root = z
    }

    fun size(): Int = size

    fun leftmost(node: RedBlackTree<V, V>.Node?): RedBlackTree<V, V>.Node? {
        var node = node
        while (node?.left.isNotNil()) {
            node = node!!.left!!
        }
        return node
    }

    fun rightmost(node: RedBlackTree<V, V>.Node?): RedBlackTree<V, V>.Node? {
        var node = node
        while (node?.right.isNotNil()) {
            node = node!!.right!!
        }
        return node
    }

    fun prevNode(node: Node): Node? {
        if (node.left.isNotNil()) {
            return rightmost(node.left!!)
        }
        var node = node
        var parent = node.parent
        while (parent.isNotNil() && parent!!.left === node) {
            node = parent!!
            parent = node.parent
        }
        return parent.takeIf { it.isNotNil() }
    }

    fun nextNode(node: Node): Node? {
        if (node.right.isNotNil()) {
            return leftmost(node.right!!)
        }
        var node = node
        var parent = node.parent
        while (parent.isNotNil() && parent!!.right === node) {
            node = parent!!
            parent = node.parent
        }
        return parent.takeIf { it.isNotNil() }
    }

    fun debugTree(prepend: String = "    "): String = buildString {
        fun visit(node: Node?): String {
            val nodeValue = node?.value as DebuggableNode2<V>?
            val key = nodeValue?.debugKey().toString()
            if (node === root) {
                appendLine("$prepend$key[/\"${nodeValue?.debugLabel(node)}\"\\]")
            } else {
                appendLine("$prepend$key[\"${nodeValue?.debugLabel(node)}\"]")
            }
            node?.left.takeIf { it.isNotNil() }?.also { appendLine("$prepend$key--L-->${visit(it)}") }
            node?.right.takeIf { it.isNotNil() }?.also { appendLine("$prepend$key--R-->${visit(it)}") }
//            node.parent.takeIf { it.isNotNil() }?.also { appendLine("$prepend$key--P-->${node.parent.value.debugKey()}") }
            return key
        }
        visit(root)
    }

    // Length Tree

    fun findNodeByCharIndex(index: Int, isIncludeMarkerNodes: Boolean = true, isExact: Boolean = false): RedBlackTree<V, V>.Node? {
        var find = index
        var lastMatch: RedBlackTree<V, V>.Node? = null
        return findNode {
            when (find) {
                in Int.MIN_VALUE until it.value.leftStringLength -> -1
                it.value.leftStringLength, in it.value.leftStringLength until it.value.leftStringLength + it.value.bufferLength -> {
                    lastMatch = it
                    if (!isExact && isIncludeMarkerNodes && find == it.value.leftStringLength && it.left.isNotNil()) {
                        -1
                    } else if (!isExact && !isIncludeMarkerNodes && find == it.value.leftStringLength + it.value.bufferLength && it.right.isNotNil()) {
                        1
                    } else {
                        0
                    }
                }
                in it.value.leftStringLength + it.value.bufferLength until Int.MAX_VALUE -> (
                    if (it.right.isNotNil()) {
                        1
                    } else {
                        0
                    }
                )
                else -> throw IllegalStateException("what is find? $find")
            }.also { compareResult ->
                val isTurnRight = compareResult > 0
                if (isTurnRight) {
                    find -= it.value.leftStringLength + it.value.bufferLength
                }
            }
        }?.takeIf {
            val nodePosStart = findPositionStart(it)
            nodePosStart <= index && (
                index < nodePosStart + it.value.bufferLength
                    || it.value.bufferLength == 0
                    || (index == getRoot().length() && it === rightmost(getRoot()))
            )
        }
            ?: lastMatch
    }

    fun findNodeByRenderCharIndex(index: Int, isIncludeMarkerNodes: Boolean = true, isExact: Boolean = true): RedBlackTree<V, V>.Node? {
        var find = index
        var lastMatch: RedBlackTree<V, V>.Node? = null
        return findNode {
            when (find) {
                in Int.MIN_VALUE until it.value.leftRenderLength -> -1
                in it.value.leftRenderLength until it.value.leftRenderLength + it.value.currentRenderLength -> {
                    lastMatch = it
                    if (isExact) {
                        0
                    } else {
                        if (isIncludeMarkerNodes && find == it.value.leftRenderLength && it.left.isNotNil()) {
                            -1
                        } else if (!isIncludeMarkerNodes && find == it.value.leftRenderLength + it.value.currentRenderLength && it.right.isNotNil()) {
                            1
                        } else {
                            0
                        }
                    }
                }
                in it.value.leftRenderLength + it.value.currentRenderLength until Int.MAX_VALUE -> (
                    if (it.right.isNotNil()) {
                        1
                    } else {
                        0
                    }
                ).also { compareResult ->
                    val isTurnRight = compareResult > 0
                    if (isTurnRight) {
                        find -= it.value.leftRenderLength + it.value.currentRenderLength
                    }
                }
                else -> throw IllegalStateException("what is find? $find")
            }
        }?.takeIf {
            val nodePosStart = findRenderPositionStart(it)
            nodePosStart <= index && (
                    index < nodePosStart + it.value.currentRenderLength
                            || (isIncludeMarkerNodes && it.value.currentRenderLength == 0)
                            || (index == getRoot().renderLength() && it === rightmost(getRoot()))
                    )
        } ?: lastMatch
    }

    fun findPositionStart(node: RedBlackTree<V, V>.Node): Int {
        var start = node.value.leftStringLength
        var node = node
        while (node.parent.isNotNil()) {
            if (node === node.parent!!.right) {
                start += node.parent!!.value.leftStringLength + node.parent!!.value.bufferLength
            }
            node = node.parent!!
        }
        return start
    }

    fun findRenderPositionStart(node: RedBlackTree<V, V>.Node): Int {
        var start = node.value.leftRenderLength
        var node = node
        while (node.parent.isNotNil()) {
            if (node === node.parent!!.right) {
                start += node.parent!!.value.leftRenderLength + node.parent!!.value.currentRenderLength
            }
            node = node.parent!!
        }
        return start
    }
}

interface DebuggableNode2<T : Comparable<T>> {
    fun debugKey(): String
    fun debugLabel(node: RedBlackTree<T, T>.Node?): String
    fun attach(node: Any)
    fun detach()
}

interface RedBlackTreeComputations2<T : Comparable<T>> {
    fun recomputeFromLeaf(it: RedBlackTree<T, T>.Node)
    fun computeWhenLeftRotate(x: T, y: T)
    fun computeWhenRightRotate(x: T, y: T)
//    fun transferComputeResultTo(from: T, to: T)
}

// RB Tree Supplement

inline fun <V: Comparable<V>> RedBlackTree<V, V>.Node?.isNotNil(): Boolean = this != null

inline fun <V: Comparable<V>> RedBlackTree<V, V>.Node?.getValue() = this?.value
inline fun <V: Comparable<V>> RedBlackTree<V, V>.Node?.getRight() = this?.right
inline fun <V: Comparable<V>> RedBlackTree<V, V>.Node?.getLeft() = this?.left

// Length Tree

fun <V> RedBlackTree<V, V>.Node?.length(): Int where V : LengthNodeValue, V : Comparable<V> =
    (getValue()?.leftStringLength ?: 0) +
            (getValue()?.bufferLength ?: 0) +
            (getRight().takeIf { it.isNotNil() }?.length() ?: 0)

fun <V> RedBlackTree<V, V>.Node?.renderLength(): Int where V : LengthNodeValue, V : Comparable<V> =
    (getValue()?.leftRenderLength ?: 0) +
            (getValue()?.currentRenderLength ?: 0) +
            (getRight().takeIf { it.isNotNil() }?.renderLength() ?: 0)

fun <V> RedBlackTree<V, V>.Node?.overallLength(): Int where V : LengthNodeValue, V : Comparable<V> =
    (getValue()?.leftOverallLength ?: 0) +
            (getValue()?.currentOverallLength ?: 0) +
            (getRight().takeIf { it.isNotNil() }?.overallLength() ?: 0)
