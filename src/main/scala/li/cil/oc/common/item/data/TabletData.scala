package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.common.Tier
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag

class TabletData extends ItemData(Constants.ItemName.Tablet) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
  }

  var items = Array.fill[ItemStack](32)(ItemStack.EMPTY)
  var isRunning = false
  var energy = 0.0
  var maxEnergy = 0.0
  var tier = Tier.One
  var container = ItemStack.EMPTY

  private final val ItemsTag = Settings.namespace + "items"
  private final val SlotTag = "slot"
  private final val ItemTag = "item"
  private final val IsRunningTag = Settings.namespace + "isRunning"
  private final val EnergyTag = Settings.namespace + "energy"
  private final val MaxEnergyTag = Settings.namespace + "maxEnergy"
  private final val TierTag = Settings.namespace + "tier"
  private final val ContainerTag = Settings.namespace + "container"

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.getList(ItemsTag, Tag.TAG_COMPOUND).foreach((slotNbt: CompoundTag) => {
      val slot = slotNbt.getByte(SlotTag)
      if (slot >= 0 && slot < items.length) {
        items(slot) = ItemStack.CODEC.parse(NbtOps.INSTANCE, slotNbt.getCompound(ItemTag)).result().orElse(ItemStack.EMPTY)
      }
    })
    isRunning = nbt.getBoolean(IsRunningTag)
    energy = nbt.getDouble(EnergyTag)
    maxEnergy = nbt.getDouble(MaxEnergyTag)
    tier = nbt.getInt(TierTag)
    if (nbt.contains(ContainerTag)) {
      container = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(ContainerTag)).result().orElse(ItemStack.EMPTY)
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.setNewTagList(ItemsTag,
      items.zipWithIndex collect {
        case (stack, slot) if !stack.isEmpty => (stack, slot)
      } map {
        case (stack, slot) =>
          val slotNbt = new CompoundTag()
          slotNbt.putByte(SlotTag, slot.toByte)
          slotNbt.put(ItemTag, stack.save(provider))
      })
    nbt.putBoolean(IsRunningTag, isRunning)
    nbt.putDouble(EnergyTag, energy)
    nbt.putDouble(MaxEnergyTag, maxEnergy)
    nbt.putInt(TierTag, tier)
    if (!container.isEmpty) nbt.put(ContainerTag, container.save(provider))
  }
}
