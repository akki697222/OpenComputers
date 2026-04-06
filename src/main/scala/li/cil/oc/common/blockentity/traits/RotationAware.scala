package li.cil.oc.common.blockentity.traits

import net.minecraft.core.Direction

trait RotationAware extends TileEntity {
  def toLocal(value: Direction) = value

  def toGlobal(value: Direction) = value
}
