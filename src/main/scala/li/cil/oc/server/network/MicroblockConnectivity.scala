package li.cil.oc.server.network

import li.cil.oc.common.blockentity.traits.ImmibisMicroblock
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity

object MicroblockConnectivity {
  def canConnectFromSide(tileEntity: BlockEntity, side: Direction): Boolean =
    if (tileEntity == null) true
    else tileEntity match {
      case im: ImmibisMicroblock => im.ImmibisMicroblocks_isSideOpen(side.get3DDataValue)
      case _ => true
    }
}
