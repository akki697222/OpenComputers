package li.cil.oc.common.blockentity

import li.cil.oc.Settings
import li.cil.oc.common.datacomponents.{OCComponents, VideoMode}
import li.cil.oc.util.ExtendedDataComponentHolder._
import net.minecraft.core.component.DataComponentHolder
import net.minecraft.core.{BlockPos, Direction, HolderLookup}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.common.MutableDataComponentHolder

class HoloScreen(pos: BlockPos, state: BlockState, tier: Int) extends Screen(pos, state, tier) {
  private final val ConfigWidthTag = Settings.namespace + "configWidth"
  private final val ConfigHeightTag = Settings.namespace + "configHeight"

  shouldCheckForMultiBlock = false
  delayUntilCheckForMultiBlock = 0

  override def checkMultiBlock(): Unit = {
    shouldCheckForMultiBlock = false
    origin = this
    screens.clear()
    screens += this
    //cachedBounds = None
  }

  def resize(side: Direction, facing: Direction): Boolean = {
    val oldWidth = width
    val oldHeight = height
    side match {
      case Direction.UP =>
        height = (height + 1) min Settings.get.maxScreenHeight
      case Direction.DOWN =>
        height = (height - 1) max 1
      case value if facing == clockwise(value) =>
        width = (width + 1) min Settings.get.maxScreenWidth
      case value if facing == counterClockwise(value) =>
        width = (width - 1) max 1
      case value if value == facing =>
        height = (height - 1) max 1
      case _ =>
    }
    if (oldWidth != width || oldHeight != height) {
      buffer.setAspectRatio(width, height)
      //cachedBounds = None
      setChanged()
      if (getLevel != null && !getLevel.isClientSide) {
        getLevel.sendBlockUpdated(getBlockPos, getBlockState, getBlockState, 3)
      }
      true
    }
    else false
  }

  override def loadComponentsCommon(holder: DataComponentHolder): Unit = {
    super.loadComponentsCommon(holder)
    for(VideoMode(w, h) <- holder.getComponent(OCComponents.VIDEO_MODE)) {
      width = w max 1 min Settings.get.maxScreenWidth
      height = h max 1 min Settings.get.maxScreenHeight
      checkMultiBlock()
    }
  }

  override def saveComponentsCommon(holder: MutableDataComponentHolder): Unit = {
    super.saveComponentsCommon(holder)
    holder.setComponent(OCComponents.VIDEO_MODE, VideoMode(width, height))
  }

  private def clockwise(facing: Direction): Direction = facing match {
    case Direction.NORTH => Direction.EAST
    case Direction.EAST => Direction.SOUTH
    case Direction.SOUTH => Direction.WEST
    case Direction.WEST => Direction.NORTH
    case other => other
  }

  private def counterClockwise(facing: Direction): Direction = facing match {
    case Direction.NORTH => Direction.WEST
    case Direction.WEST => Direction.SOUTH
    case Direction.SOUTH => Direction.EAST
    case Direction.EAST => Direction.NORTH
    case other => other
  }
}
