package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api.ImmutableItemStack
import li.cil.oc.common.datacomponents.OCComponents
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ExtendedItemStack._
import li.cil.oc.util.ExtendedDataComponentHolder._
import li.cil.oc.util.ItemUtils
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.{DataComponentHolder, DataComponents}
import net.minecraft.world.item.{ItemStack, Items, MapItem}
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import net.neoforged.neoforge.common.MutableDataComponentHolder
import net.neoforged.neoforge.server.ServerLifecycleHooks

class NavigationUpgradeData extends ItemData(Constants.ItemName.NavigationUpgrade) {
  def this(stack: DataComponentHolder) = {
    this()
    loadData(stack)
  }

  var map = new ItemStack(Items.FILLED_MAP)

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

  override def loadData(holder: DataComponentHolder): Unit = {
    map = holder.getComponent(OCComponents.SOURCE_MAP_ITEM).map(_.mutableCopy()).orNull
  }

  override def saveData(holder: MutableDataComponentHolder): Unit = {
    if(map != null) {
      holder.setComponent(OCComponents.SOURCE_MAP_ITEM, ImmutableItemStack.copyOf(map))
    }
  }
}
