package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ItemUtils
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class HoverBootsData extends ItemData(Constants.ItemName.HoverBoots) {
  def this(stack: ItemStack) = {
    this()
    ItemUtils.getTag(stack) match {
      case tag: CompoundTag => loadData(tag)
    }
  }

  var charge = 0.0

  private final val ChargeTag = Settings.namespace + "charge"

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    loadData(nbt)
  }

  def loadData(nbt: CompoundTag): Unit = {
    charge = nbt.getDouble(ChargeTag)
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    saveData(nbt)
  }

  def saveData(nbt: CompoundTag): Unit = {
    nbt.putDouble(ChargeTag, charge)
  }
}
