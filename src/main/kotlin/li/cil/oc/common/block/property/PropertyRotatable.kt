package li.cil.oc.common.block.property

import java.util.function.Predicate
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.core.Direction

object PropertyRotatable {
    @JvmField
    val Facing: DirectionProperty = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL)

    @JvmField
    val Pitch: DirectionProperty = DirectionProperty.create("pitch", Predicate<Direction> { d ->
        d.axis == Direction.Axis.Y || d == Direction.NORTH
    })

    @JvmField
    val Yaw: DirectionProperty = DirectionProperty.create("yaw", Direction.Plane.HORIZONTAL)
}
