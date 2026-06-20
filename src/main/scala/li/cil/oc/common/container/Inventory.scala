package li.cil.oc.common.container

import li.cil.oc.Settings
import li.cil.oc.api.{ImmutableItemStack, Persistable}
import li.cil.oc.common.datacomponents.OCComponents
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ExtendedDataComponentHolder._
import li.cil.oc.util.StackOption
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.{DataComponentHolder, DataComponentType}
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.nbt.Tag
import net.neoforged.neoforge.common.MutableDataComponentHolder

trait Inventory extends SimpleInventory with Persistable {
  def items: Array[ItemStack]

  def updateItems(slot: Int, stack: ItemStack): Unit = items(slot) = StackOption(stack).orEmpty

  // ----------------------------------------------------------------------- //

  override def getItem(slot: Int): ItemStack =
    if (slot >= 0 && slot < getContainerSize) items(slot)
    else ItemStack.EMPTY

  override def setItem(slot: Int, stack: ItemStack): Unit = {
    if (slot >= 0 && slot < getContainerSize) {
      if (stack.isEmpty && items(slot).isEmpty) {
        return
      }
      if (items(slot) == stack) {
        return
      }

      val oldStack = items(slot)
      updateItems(slot, ItemStack.EMPTY)
      if (!oldStack.isEmpty) {
        onItemRemoved(slot, oldStack)
      }
      if (!stack.isEmpty && stack.getCount >= getInventoryStackRequired) {
        if (stack.getCount > getMaxStackSize) {
          stack.setCount(getMaxStackSize)
        }
        updateItems(slot, stack)
      }

      if (!items(slot).isEmpty) {
        onItemAdded(slot, items(slot))
      }

      setChanged()
    }
  }

  override def getName: Component = Component.translatable(Settings.namespace + "container." + inventoryName)

  protected def inventoryName: String = getClass.getSimpleName.toLowerCase

  override def isEmpty: Boolean = items.forall(_.isEmpty)

  // ----------------------------------------------------------------------- //

  def loadFrom(value: Iterable[ImmutableItemStack]): Unit = {
    for (item <- value; i <- 0 until (value.size max items.length)) {
      items(i) = item.mutableCopy()
    }
  }

  def component: DataComponentType[List[ImmutableItemStack]] =
    OCComponents.CONTENTS.get()

  override def loadData(holder: DataComponentHolder): Unit = {
    for(items <- holder.getComponent(this.component)) {
      loadFrom(items)
    }
  }

  override def saveData(holder: MutableDataComponentHolder): Unit = {
    holder.setComponent(this.component, items.map(ImmutableItemStack.copyOf).toList)
  }

  // ----------------------------------------------------------------------- //

  protected def onItemAdded(slot: Int, stack: ItemStack): Unit = {}

  protected def onItemRemoved(slot: Int, stack: ItemStack): Unit = {}
}
