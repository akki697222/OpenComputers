package li.cil.oc.common.blockentity

import li.cil.oc.server.component
import net.minecraft.core.{BlockPos, HolderLookup}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class Geolyzer(pos: BlockPos, state: BlockState) 
  extends BlockEntity(TileEntityTypes.GEOLYZER.get(), pos, state) with traits.Environment {
  val geolyzer = new component.Geolyzer(this)

  def node = geolyzer.node

  override def loadForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadForServer(nbt, provider)
    geolyzer.loadData(nbt, provider)
  }

  override def saveForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveForServer(nbt, provider)
    geolyzer.saveData(nbt, provider)
  }
}
