package li.cil.oc.common.item.data

import li.cil.oc.common.nanomachines.ControllerImpl
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ItemUtils
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class NanomachineData extends ItemData(Constants.ItemName.Nanomachines) {
  def this(stack: ItemStack) = {
    this()
    val nbt = ItemUtils.getTag(stack)
    if(nbt != null) {
      loadData(nbt)
    }
  }

  def this(controller: ControllerImpl) = {
    this()
    uuid = controller.uuid
    val nbt = new CompoundTag()
    controller.configuration.saveData(nbt, forItem = true)
    configuration = Option(nbt)
  }

  var uuid = ""
  var configuration: Option[CompoundTag] = None

  private final val UUIDTag = Settings.namespace + "uuid"
  private final val ConfigurationTag = Settings.namespace + "configuration"

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    loadData(nbt)
  }

  def loadData(nbt: CompoundTag): Unit = {
    uuid = nbt.getString(UUIDTag)
    if (nbt.contains(ConfigurationTag)) {
      configuration = Option(nbt.getCompound(ConfigurationTag))
    }
    else {
      configuration = None
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    saveData(nbt)
  }

  def saveData(nbt: CompoundTag): Unit = {
    nbt.putString(UUIDTag, uuid)
    configuration.foreach(nbt.put(ConfigurationTag, _))
  }
}
