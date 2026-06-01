package li.cil.oc.util

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RTree<Data : Any>(private val maxEntries: Int, private val coordinate: (Data) -> Triple<Double, Double, Double>) {
    init {
        require(maxEntries >= 2) { "maxEntries must be larger or equal to 2." }
    }

    private val entries = ConcurrentHashMap<Data, Leaf>()
    private val minEntries = max(maxEntries / 2, 1)
    private var root: Node = NonLeaf()

    @Synchronized
    fun apply(value: Data): Triple<Double, Double, Double>? {
        return entries[value]?.bounds?.min?.asTuple()
    }

    @Synchronized
    fun allBounds(): List<Pair<Pair<Triple<Double, Double, Double>, Triple<Double, Double, Double>>, Int>> {
        return root.allBounds(0).toList()
    }

    @Synchronized
    fun add(value: Data): Boolean {
        val replaced = remove(value)
        val entry = Leaf(value, Point(coordinate(value)))
        entries[value] = entry
        val newNode = root.add(entry)
        if (newNode !== root) {
            root = NonLeaf(newNode, root)
        }
        return !replaced
    }

    @Synchronized
    fun remove(value: Data): Boolean {
        val node = entries.remove(value) ?: return false
        root.remove(node)
        val children = (root as NonLeaf).children
        if (children.size == 1) {
            val first = children.first()
            if (first is NonLeaf) {
                root = first
            } else {
                (root as NonLeaf).updateBounds()
            }
        } else {
            (root as NonLeaf).updateBounds()
        }
        return true
    }

    @Synchronized
    fun query(from: Triple<Double, Double, Double>, to: Triple<Double, Double, Double>): Iterable<Data> {
        return root.query(Rectangle(Point(from), Point(to)))
    }

    private abstract inner class Node {
        abstract var bounds: Rectangle
        open fun allBounds(level: Int): Iterable<Pair<Pair<Triple<Double, Double, Double>, Triple<Double, Double, Double>>, Int>> {
            return listOf(bounds.asTuple() to level)
        }

        open val isLeaf: Boolean get() = true
        abstract fun add(value: Node): Node
        abstract fun remove(value: Node): Node?
        abstract fun query(query: Rectangle): Iterable<Data>
    }

    private inner class NonLeaf : Node {
        val children = mutableSetOf<Node>()
        override var bounds = Rectangle(Point.PositiveInfinity, Point.NegativeInfinity)

        constructor(vararg nodes: Node) : super() {
            for (child in nodes) {
                children.add(child)
                bounds = bounds.including(child.bounds)
            }
        }

        override fun allBounds(level: Int): Iterable<Pair<Pair<Triple<Double, Double, Double>, Triple<Double, Double, Double>>, Int>> {
            return super.allBounds(level) + children.flatMap { it.allBounds(level + 1) }
        }

        override val isLeaf: Boolean get() = children.firstOrNull() is Leaf

        override fun add(value: Node): Node {
            assert(value !== this)
            uncheckedAdd(value)
            return if (children.size > maxEntries) {
                split()
            } else {
                bounds = bounds.including(value.bounds)
                this
            }
        }

        private fun uncheckedAdd(value: Node) {
            var bestChild: Node? = null
            var bestGrowth = Double.POSITIVE_INFINITY
            var bestVolume = Double.POSITIVE_INFINITY
            for (child in children) {
                if (!child.isLeaf || value is Leaf) {
                    val oldVolume = child.bounds.volume()
                    val volume = child.bounds.including(value.bounds).volume()
                    val growth = volume - oldVolume
                    if (growth < bestGrowth || (growth == bestGrowth && volume < bestVolume)) {
                        bestChild = child
                        bestGrowth = growth
                        bestVolume = volume
                    }
                }
            }

            if (bestChild != null) {
                children.remove(bestChild)
                children.add(bestChild.add(value))
            } else {
                children.add(value)
            }
        }

        override fun remove(value: Node): Node? {
            if (bounds.intersects(value.bounds)) {
                val iterator = children.iterator()
                while (iterator.hasNext()) {
                    val child = iterator.next()
                    val change = child.remove(value)
                    if (change != null) {
                        if (change === child) {
                            iterator.remove()
                            when (child) {
                                is NonLeaf -> {
                                    for (grandChild in child.children) {
                                        uncheckedAdd(grandChild)
                                    }
                                    if (children.size > maxEntries) {
                                        return split()
                                    }
                                }
                                is Leaf -> assert(child === value)
                            }
                            if (children.size < minEntries) {
                                return this
                            }
                            updateBounds()
                            return value
                        } else if (change === value) {
                            updateBounds()
                            return value
                        } else {
                            assert(change is RTree<*>.NonLeaf)
                            uncheckedAdd(change)
                            if (children.size > maxEntries) {
                                return split()
                            } else {
                                updateBounds()
                                return value
                            }
                        }
                    }
                }
            }
            return null
        }

        fun updateBounds() {
            bounds = Rectangle.around(children)
        }

        override fun query(query: Rectangle): Iterable<Data> {
            return if (query.intersects(bounds)) {
                children.flatMap { it.query(query) }
            } else {
                emptyList()
            }
        }

        private fun split(): NonLeaf {
            val values = children.toTypedArray()
            var seed1: Node? = null
            var seed2: Node? = null
            var worst = Double.NEGATIVE_INFINITY
            for (i in values.indices) {
                val si = values[i]
                for (j in i + 1 until values.size) {
                    val sj = values[j]
                    val vol1 = si.bounds.volume()
                    val vol2 = sj.bounds.volume()
                    val vol = si.bounds.including(sj.bounds).volume()
                    val d = vol - vol1 - vol2
                    if (d > worst) {
                        seed1 = si
                        seed2 = sj
                        worst = d
                    }
                }
            }

            if (seed1 != null && seed2 != null) {
                val r1 = SplitResult(mutableSetOf(seed1), seed1.bounds)
                val r2 = SplitResult(mutableSetOf(seed2), seed2.bounds)

                val list = values.toMutableSet()
                list.remove(seed1)
                list.remove(seed2)
                while (list.isNotEmpty()) {
                    if (minEntries - r1.set.size >= list.size) {
                        for (item in list) r1.add(item)
                        list.clear()
                    } else if (minEntries - r2.set.size >= list.size) {
                        for (item in list) r2.add(item)
                        list.clear()
                    } else {
                        var bestValue: Node? = null
                        var r = r1
                        var bestD = Double.NEGATIVE_INFINITY
                        for (value in list) {
                            val newVol1 = r1.volumeIncluding(value)
                            val newVol2 = r2.volumeIncluding(value)
                            val growth1 = newVol1 - r1.volume()
                            val growth2 = newVol2 - r2.volume()
                            val d = abs(growth2 - growth1)
                            if (d > bestD) {
                                bestValue = value
                                r = if (growth1 < growth2 || (growth1 == growth2 && newVol1 < newVol2)) r1 else r2
                                bestD = d
                            }
                        }
                        bestValue?.let {
                            list.remove(it)
                            r.add(it)
                        } ?: break
                    }
                }

                children.clear()
                children.addAll(r1.set)
                bounds = r1.bounds

                val ll = NonLeaf()
                ll.children.addAll(r2.set)
                ll.bounds = r2.bounds
                return ll
            }
            throw AssertionError()
        }
    }

    private inner class Leaf(val data: Data, point: Point) : Node() {
        override var bounds = Rectangle(point, point)

        override fun add(value: Node) = value

        override fun remove(value: Node) = if (value === this) this else null

        override fun query(query: Rectangle) = if (query.intersects(bounds)) listOf(data) else emptyList()
    }

    private class Point(val x: Double, val y: Double, val z: Double) {
        constructor(p: Triple<Double, Double, Double>) : this(p.first, p.second, p.third)

        fun min(other: Point) = Point(min(x, other.x), min(y, other.y), min(z, other.z))
        fun max(other: Point) = Point(max(x, other.x), max(y, other.y), max(z, other.z))
        fun asTuple() = Triple(x, y, z)

        companion object {
            val NegativeInfinity = Point(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
            val PositiveInfinity = Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        }
    }

    private inner class Rectangle(val min: Point, val max: Point) {
        fun including(value: Rectangle) = Rectangle(value.min.min(min), value.max.max(max))

        fun intersects(value: Rectangle) =
            value.min.x <= max.x && value.min.y <= max.y && value.min.z <= max.z &&
                    value.max.x >= min.x && value.max.y >= min.y && value.max.z >= min.z

        fun volume(): Double {
            val sx = max.x - min.x
            val sy = max.y - min.y
            val sz = max.z - min.z
            return sx * sy * sz
        }

        fun asTuple() = min.asTuple() to max.asTuple()

        companion object {
            fun around(values: Iterable<Node>): Rectangle {
                var min = Point.PositiveInfinity
                var max = Point.NegativeInfinity
                for (value in values) {
                    min = value.bounds.min.min(min)
                    max = value.bounds.max.max(max)
                }
                return Rectangle(min, max)
            }
        }
    }

    private inner class SplitResult(val set: MutableSet<Node>, var bounds: Rectangle) {
        fun add(value: Node) {
            set.add(value)
            bounds = bounds.including(value.bounds)
        }

        fun volume() = bounds.volume()
        fun volumeIncluding(value: Node) = bounds.including(value.bounds).volume()
    }
}
