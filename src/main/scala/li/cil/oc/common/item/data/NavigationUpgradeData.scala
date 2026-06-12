package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ItemUtils
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.MapItem
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import net.neoforged.neoforge.server.ServerLifecycleHooks

class NavigationUpgradeData extends ItemData(Constants.ItemName.NavigationUpgrade) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack, ServerLifecycleHooks.getCurrentServer.registryAccess())
  }

  var map = new ItemStack(net.minecraft.world.item.Items.FILLED_MAP)

  def mapData(level: Level): MapItemSavedData = {
    val data = MapItem.getSavedData(map, level)
    if (data == null) {
      throw new Exception("invalid map")
    }
    data
  }

  def getSize(level: Level) = {
    val info = mapData(level)
    128 * (1 << info.scale)
  }

  private final val DataTag = Settings.namespace + "data"
  private final val MapTag = Settings.namespace + "map"

  override def loadData(stack: ItemStack, provider: HolderLookup.Provider): Unit = {
    ItemUtils.getTag(stack) match {
      case tag: CompoundTag => loadData(tag.getCompound(DataTag), provider)
    }
  }

  override def saveData(stack: ItemStack, provider: HolderLookup.Provider): Unit = {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, data => {
      if (!data.contains(DataTag)) {
        data.put(DataTag, new CompoundTag())
      }

      saveData(data.getCompound(DataTag), provider)
    })
  }

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    if (nbt.contains(MapTag)) {
      map = ItemStack.parse(provider, nbt.getCompound(MapTag)).get()
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    if (map != null) {
      nbt.setNewCompoundTag(MapTag, tag => map.save(provider, tag))
    }
  }
}
