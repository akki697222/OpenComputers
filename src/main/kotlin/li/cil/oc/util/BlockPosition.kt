package li.cil.oc.util

import com.google.common.hash.Hashing
import li.cil.oc.api.network.EnvironmentHost
import net.minecraft.world.level.Level
import net.minecraft.core.Direction
import net.minecraft.world.phys.AABB
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import scala.Option

class BlockPosition(
    @JvmField val x: Int,
    @JvmField val y: Int,
    @JvmField val z: Int,
    @JvmField val world: Option<Level>
) {
    // Auxiliary constructors
    constructor(x: Int, y: Int, z: Int, world: Level?) : this(x, y, z, Option.apply(world))

    constructor(x: Int, y: Int, z: Int) : this(x, y, z, Option.empty<Level>())

    constructor(x: Double, y: Double, z: Double, world: Option<Level>) : this(
        kotlin.math.floor(x).toInt(),
        kotlin.math.floor(y).toInt(),
        kotlin.math.floor(z).toInt(),
        world
    )

    constructor(x: Double, y: Double, z: Double, world: Level?) : this(
        x, y, z, Option.apply(world)
    )

    constructor(x: Double, y: Double, z: Double) : this(
        x, y, z, Option.empty<Level>()
    )

    fun offset(direction: Direction, n: Int): BlockPosition = BlockPosition(
        x + direction.getStepX() * n,
        y + direction.getStepY() * n,
        z + direction.getStepZ() * n,
        world
    )

    fun offset(direction: Direction): BlockPosition = offset(direction, 1)

    fun offset(x: Double, y: Double, z: Double): Vec3 = Vec3(this.x + x, this.y + y, this.z + z)

    val bounds: AABB
        get() = AABB(x.toDouble(), y.toDouble(), z.toDouble(), (x + 1).toDouble(), (y + 1).toDouble(), (z + 1).toDouble())

    fun toBlockPos(): BlockPos = BlockPos(x, y, z)

    fun toVec3(): Vec3 = Vec3(x + 0.5, y + 0.5, z + 0.5)

    override fun equals(other: Any?): Boolean {
        if (other is BlockPosition) {
            return other.x == x && other.y == y && other.z == z && other.world == world
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return Hashing.goodFastHash(32)
            .newHasher(16)
            .putInt(x)
            .putInt(y)
            .putInt(z)
            .putInt(world.hashCode())
            .hash()
            .asInt()
    }

    companion object {
        @JvmStatic
        fun apply(x: Int, y: Int, z: Int, world: Level): BlockPosition =
            BlockPosition(x, y, z, Option.apply(world))

        @JvmStatic
        fun apply(x: Int, y: Int, z: Int): BlockPosition =
            BlockPosition(x, y, z, Option.empty<Level>())

        @JvmStatic
        fun apply(x: Double, y: Double, z: Double, world: Level): BlockPosition =
            BlockPosition(x, y, z, Option.apply(world))

        @JvmStatic
        fun apply(x: Double, y: Double, z: Double): BlockPosition =
            BlockPosition(x, y, z, Option.empty<Level>())

        @JvmStatic
        fun apply(v: Vec3): BlockPosition =
            BlockPosition(v.x, v.y, v.z, Option.empty<Level>())

        @JvmStatic
        fun apply(v: Vec3, world: Level): BlockPosition =
            BlockPosition(v.x, v.y, v.z, Option.apply(world))

        @JvmStatic
        fun apply(host: EnvironmentHost): BlockPosition =
            apply(host.xPosition(), host.yPosition(), host.zPosition(), host.getEnvironmentLevel())

        @JvmStatic
        fun apply(entity: Entity): BlockPosition =
            apply(entity.x, entity.y, entity.z, entity.level)

        @JvmStatic
        fun apply(pos: BlockPos, world: Level): BlockPosition =
            apply(pos.x, pos.y, pos.z, world)

        @JvmStatic
        fun apply(pos: BlockPos): BlockPosition =
            apply(pos.x, pos.y, pos.z)
    }
}
