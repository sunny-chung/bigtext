package com.sunnychung.lib.multiplatform.bigtext.redblacktree

enum class Color { RED, BLACK }

open class RedBlackTree<K : Comparable<K>, V> : Iterable<V> {
    inner class Node(
        key: K,
        value: V,
        color: Color,
        left: Node? = null,
        right: Node? = null,
        parent: Node? = null
    ) {
        var parent = parent
            internal set
        var right = right
            internal set
        var left = left
            internal set
        var color = color
            internal set
        var value = value
            internal set
        var key = key
            internal set
    }

    var root: Node? = null
        protected set

    // --- Public API ---

    val isEmpty: Boolean
        get() = root == null

    var size: Int = 0
        protected set

    fun get(key: K): V? = searchNode(key)?.value

    fun contains(key: K): Boolean = searchNode(key) != null

    open fun insert(key: K, value: V): Node? {
        var y: Node? = null
        var x = root

        while (x != null) {
            y = x
            when {
                key < x.key -> x = x.left
                key > x.key -> x = x.right
                else -> {
                    x.value = value
                    return null
                }
            }
        }

        val z = Node(key, value, Color.RED, null, null, y)
        if (y == null) {
            root = z
        } else if (key < y.key) {
            y.left = z
        } else {
            y.right = z
        }

        insertFixup(z)
        ++size
        return z
    }

    fun remove(key: K) {
        val z = searchNode(key) ?: return
        deleteNode(z)
    }

    fun inOrder(): List<Pair<K, V>> {
        val result = mutableListOf<Pair<K, V>>()
        inOrder(root, result)
        return result
    }

    fun findNode(comparison: (Node) -> Int): Node? {
        var x = root
        while (x != null) {
            val comparisonResult = comparison(x)
            when {
                comparisonResult < 0 -> x = x.left
                comparisonResult > 0 -> x = x.right
                else -> return x
            }
        }
        return null
    }

    fun find(comparison: (Node) -> Int): V? = findNode(comparison)?.value

    override fun iterator(): Iterator<V> = object : Iterator<V> {
        private val stack = mutableListOf<RedBlackTree<K, V>.Node>()
        private var current = root

        init {
            // Push all the way to the left
            while (current != null) {
                stack.add(current!!)
                current = current!!.left
            }
            current = null
        }

        override fun hasNext(): Boolean = stack.isNotEmpty()

        override fun next(): V {
            if (!hasNext()) throw NoSuchElementException()
            val node = stack.removeAt(stack.lastIndex)
            var nextNode = node.right
            while (nextNode != null) {
                stack.add(nextNode)
                nextNode = nextNode.left
            }
            return node.value
        }
    }

    fun visitInPostOrder(visitor: (Node) -> Unit) {
        fun visit(node: Node?) {
            if (node == null) return
            visit(node.left)
            visit(node.right)
            visitor(node)
        }
        visit(root)
    }

    // --- Internal helpers ---

    private fun inOrder(node: Node?, list: MutableList<Pair<K, V>>) {
        if (node != null) {
            inOrder(node.left, list)
            list.add(node.key to node.value)
            inOrder(node.right, list)
        }
    }

    private fun searchNode(key: K): Node? {
        var x = root
        while (x != null) {
            when {
                key < x.key -> x = x.left
                key > x.key -> x = x.right
                else -> return x
            }
        }
        return null
    }

    protected fun insertFixup(z: Node) {
        var z = z
        while (z.parent?.color == Color.RED) {
            val p = z.parent!!
            val g = p.parent!!
            if (p == g.left) {
                val y = g.right
                if (y?.color == Color.RED) {
                    p.color = Color.BLACK
                    y.color = Color.BLACK
                    g.color = Color.RED
                    z = g
                } else {
                    if (z == p.right) {
                        z = p
                        rotateLeft(z)
                    }
                    z.parent?.color = Color.BLACK
                    g.color = Color.RED
                    rotateRight(g)
                }
            } else {
                val y = g.left
                if (y?.color == Color.RED) {
                    p.color = Color.BLACK
                    y.color = Color.BLACK
                    g.color = Color.RED
                    z = g
                } else {
                    if (z == p.left) {
                        z = p
                        rotateRight(z)
                    }
                    z.parent?.color = Color.BLACK
                    g.color = Color.RED
                    rotateLeft(g)
                }
            }
        }
        root?.color = Color.BLACK
    }

    protected open fun transplant(u: Node, v: Node?) {
        val uParent = u.parent
        if (u.parent == null) {
            root = v
        } else if (u == u.parent?.left) {
            u.parent?.left = v
        } else {
            u.parent?.right = v
        }
        v?.parent = u.parent
        uParent?.let { n -> recompute(n) }
        // the recomputation of `v` is called outside
    }

    private fun minimum(x: Node): Node {
        var curr = x
        while (curr.left != null) curr = curr.left!!
        return curr
    }

    open fun deleteNode(z: Node) {
        var y = z
        var yOriginalColor = y.color
        var x: Node? = null
        var xParent: Node? = null

        if (z.left == null) {
            x = z.right
            xParent = z.parent
            transplant(z, z.right)
            xParent?.let { n -> recomputeBottomUp(n) }
        } else if (z.right == null) {
            x = z.left
            xParent = z.parent
            transplant(z, z.left)
            xParent?.let { n -> recomputeBottomUp(n) }
        } else {
            y = minimum(z.right!!)
            yOriginalColor = y.color
            x = y.right
            xParent = y.parent
            if (y.parent == z) {
                xParent = y
            } else {
                transplant(y, y.right)
                y.right = z.right
                y.right?.parent = y
            }
            transplant(z, y)
            y.left = z.left
            y.left?.parent = y
            y.color = z.color

            recomputeBottomUp(y)
        }

        if (yOriginalColor == Color.BLACK) {
            deleteFixup(x, xParent)
        }
        --size
    }

    protected open fun recomputeBottomUp(n: Node) {
        var p: Node? = n
        while (p != null) {
            recompute(p)
            p = p.parent
        }
    }

    protected open fun recompute(n: Node) = Unit

    /**
     * x is the replacement node (may be null)
     * p is the parent of x (may be null if x == root)
     */
    private fun deleteFixup(xOrig: Node?, parentOrig: Node?) {
        var x = xOrig
        var p = parentOrig

        while ((x != root) && (x == null || x.color == Color.BLACK)) {
            if (p == null) break
            if (x == p.left) {
                var w = p.right
                if (w?.color == Color.RED) {
                    w.color = Color.BLACK
                    p.color = Color.RED
                    rotateLeft(p)
                    w = p.right
                }
                if ((w?.left?.color ?: Color.BLACK) == Color.BLACK &&
                    (w?.right?.color ?: Color.BLACK) == Color.BLACK
                ) {
                    w?.color = Color.RED
                    x = p
                    p = x.parent
                } else {
                    if ((w?.right?.color ?: Color.BLACK) == Color.BLACK) {
                        w?.left?.color = Color.BLACK
                        w?.color = Color.RED
                        if (w != null) rotateRight(w)
                        w = p.right
                    }
                    w?.color = p.color
                    p.color = Color.BLACK
                    w?.right?.color = Color.BLACK
                    rotateLeft(p)
                    x = root
                    break
                }
            } else {
                var w = p.left
                if (w?.color == Color.RED) {
                    w.color = Color.BLACK
                    p.color = Color.RED
                    rotateRight(p)
                    w = p.left
                }
                if ((w?.right?.color ?: Color.BLACK) == Color.BLACK &&
                    (w?.left?.color ?: Color.BLACK) == Color.BLACK
                ) {
                    w?.color = Color.RED
                    x = p
                    p = x.parent
                } else {
                    if ((w?.left?.color ?: Color.BLACK) == Color.BLACK) {
                        w?.right?.color = Color.BLACK
                        w?.color = Color.RED
                        if (w != null) rotateLeft(w)
                        w = p.left
                    }
                    w?.color = p.color
                    p.color = Color.BLACK
                    w?.left?.color = Color.BLACK
                    rotateRight(p)
                    x = root
                    break
                }
            }
        }
        x?.color = Color.BLACK
    }

    protected open fun rotateLeft(x: Node): Boolean {
        val y = x.right ?: return false
        x.right = y.left
        if (y.left != null) y.left?.parent = x
        y.parent = x.parent
        if (x.parent == null) {
            root = y
        } else if (x == x.parent?.left) {
            x.parent?.left = y
        } else {
            x.parent?.right = y
        }
        y.left = x
        x.parent = y
        return true
    }

    protected open fun rotateRight(x: Node): Boolean {
        val y = x.left ?: return false
        x.left = y.right
        if (y.right != null) y.right?.parent = x
        y.parent = x.parent
        if (x.parent == null) {
            root = y
        } else if (x == x.parent?.right) {
            x.parent?.right = y
        } else {
            x.parent?.left = y
        }
        y.right = x
        x.parent = y
        return true
    }

    // --- For Testing Only ---
    internal fun isValidRBTree(): Boolean = isValidRBTree(root).first

    private fun isValidRBTree(node: Node?): Pair<Boolean, Int> {
        if (node == null) return true to 1
        // Rule 1: No red node has a red child
        if (node.color == Color.RED) {
            if ((node.left?.color == Color.RED) || (node.right?.color == Color.RED)) return false to 0
        }
        val (lValid, lBlackHeight) = isValidRBTree(node.left)
        val (rValid, rBlackHeight) = isValidRBTree(node.right)
        // Rule 2: Every path from root to leaf has the same number of black nodes
        if (!lValid || !rValid || lBlackHeight != rBlackHeight) return false to 0
        return true to (lBlackHeight + if (node.color == Color.BLACK) 1 else 0)
    }
}
