package li.cil.oc.common.item.data

import com.google.common.base.Strings
import li.cil.oc.Constants
import li.cil.oc.util.ItemUtils
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class DroneData extends MicrocontrollerData(Constants.ItemName.Drone) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
  }

  var name = ""

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadData(nbt, provider)
    name = ItemUtils.getDisplayName(nbt).getOrElse("")
    if (Strings.isNullOrEmpty(name)) {
      name = RobotData.randomName
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveData(nbt, provider)
    if (!Strings.isNullOrEmpty(name)) {
      ItemUtils.setDisplayName(nbt, name)
    }
  }
}
