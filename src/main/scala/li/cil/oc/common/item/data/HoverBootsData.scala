package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class HoverBootsData extends ItemData(Constants.ItemName.HoverBoots) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
  }

  var charge = 0.0

  private final val ChargeTag = Settings.namespace + "charge"

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    charge = nbt.getDouble(ChargeTag)
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.putDouble(ChargeTag, charge)
  }
}
