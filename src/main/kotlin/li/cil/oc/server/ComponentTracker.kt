package li.cil.oc.server

import li.cil.oc.common
import net.minecraft.world.level.Level

object ComponentTracker : common.ComponentTracker() {
    override fun clear(level: Level) {
        if (!level.isClientSide) super.clear(level)
    }
}
