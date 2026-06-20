package li.cil.oc.common.blockentity.traits

import li.cil.oc.api
import net.minecraft.world.level.block.entity.BlockEntity

/** Compatibility hook for Immibis Microblocks side-open checks (parity with OC 1.12). */
trait ImmibisMicroblock extends BlockEntity {
  val ImmibisMicroblocks_TransformableTileEntityMarker = null

  def ImmibisMicroblocks_isSideOpen(side: Int): Boolean = true

  def ImmibisMicroblocks_onMicroblocksChanged(): Unit = {
    if (getLevel != null && !getLevel.isClientSide) {
      api.Network.joinOrCreateNetwork(this)
    }
  }
}
