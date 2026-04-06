package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.{BlockGetter => IBlockReader}
import net.minecraft.world.level.{Level => World}

class Transposer(props: Properties) extends SimpleBlock(props) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Transposer(pos, state)
}
