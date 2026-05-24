package li.cil.oc.client

import li.cil.oc.common
import net.minecraft.world.level.Level

object ComponentTracker : common.ComponentTracker() {
    override fun clear(world: Level) {
        if (world.isClientSide) super.clear(world)
    }
}
