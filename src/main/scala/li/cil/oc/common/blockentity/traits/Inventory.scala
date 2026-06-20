package li.cil.oc.common.blockentity.traits

import li.cil.oc.api.Persistable

import java.util.function.Consumer
import li.cil.oc.common.container
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.InventoryUtils
import net.minecraft.core.component.DataComponentHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.{Direction, HolderLookup}
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.common.MutableDataComponentHolder

trait Inventory extends BaseBlockEntity with container.Inventory {
  private lazy val inventory = Array.fill[ItemStack](getContainerSize)(ItemStack.EMPTY)

  def items = inventory

  // ----------------------------------------------------------------------- //

  override def getDisplayName: Component = super[Inventory].getDisplayName

  override def loadComponentsForServer(holder: DataComponentHolder): Unit = {
    super.loadComponentsForServer(holder)
    loadData(Persistable.holder(this))
  }

  override def saveComponentsForServer(holder: MutableDataComponentHolder): Unit = {
    super.saveComponentsForServer(holder)
    saveData(Persistable.holder(this))
  }

  override def loadForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadForServer(nbt, provider)
    loadData(nbt, provider)
  }

  override def saveForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveForServer(nbt, provider)
    saveData(nbt, provider)
  }

  // ----------------------------------------------------------------------- //

  override def stillValid(player: Player) =
    player.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= 64

  // ----------------------------------------------------------------------- //

  def forAllLoot(dst: Consumer[ItemStack]): Unit = InventoryUtils.forAllSlots(this, dst)

  def dropSlot(slot: Int, count: Int = getMaxStackSize, direction: Option[Direction] = None) =
    InventoryUtils.dropSlot(BlockPosition(x, y, z, getLevel), this, slot, count, direction)

  def dropAllSlots() =
    InventoryUtils.dropAllSlots(BlockPosition(x, y, z, getLevel), this)

  def spawnStackInWorld(stack: ItemStack, direction: Option[Direction] = None) =
    InventoryUtils.spawnStackInWorld(BlockPosition(x, y, z, getLevel), stack, direction)
}
