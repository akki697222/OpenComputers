package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.saveddata.maps.MapItemSavedData

class NavigationUpgradeData extends ItemData(Constants.ItemName.NavigationUpgrade) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
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

  override def loadData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    if (nbt.contains(MapTag)) {
      map = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound(MapTag)).result().orElse(ItemStack.EMPTY)
    }
  }

  override def saveData(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    if (map != null) {
      nbt.put(MapTag, map.save(provider))
    }
  }
}
