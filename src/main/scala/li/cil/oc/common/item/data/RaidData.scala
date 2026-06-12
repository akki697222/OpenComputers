package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag

class RaidData extends ItemData(Constants.BlockName.Raid) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
  }

  var disks = Array.empty[ItemStack]

  var filesystem = new CompoundTag()

  var label: Option[String] = None

  private final val DisksTag = Settings.namespace + "disks"
  private final val FileSystemTag = Settings.namespace + "filesystem"
  private final val LabelTag = Settings.namespace + "label"

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    disks = nbt.getList(DisksTag, Tag.TAG_COMPOUND).
      toTagArray[CompoundTag].map(tag => ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(ItemStack.EMPTY))
    filesystem = nbt.getCompound(FileSystemTag)
    if (nbt.contains(LabelTag)) {
      label = Option(nbt.getString(LabelTag))
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.setNewTagList(DisksTag, disks.toIterable)
    nbt.put(FileSystemTag, filesystem)
    label.foreach(nbt.putString(LabelTag, _))
  }
}
