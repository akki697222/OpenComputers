package li.cil.oc.common.datacomponents

import li.cil.oc.Settings
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor

object LootDiskMigrator extends Migrator[ResourceLocation] {
  override def fromNBT(tag: CompoundTag, provider: HolderLookup.Provider): Option[ResourceLocation] = {
    tag.getString(Settings.namespace + "lootFactory") match {
      case null => None
      case value: String => ResourceLocation.tryParse(value) match {
        case id: ResourceLocation => {
          tag.remove(Settings.namespace + "lootFactory")
          Some(id)
        }
        case null => None
      }
    }
  }
}

object DiskColorMigrator extends Migrator[DyeColor] {
  override def fromNBT(tag: CompoundTag, provider: HolderLookup.Provider): Option[DyeColor] = {
    if (tag.contains(Settings.namespace + "color"))
      Some(DyeColor.byId(tag.getInt(Settings.namespace + "color")))
    else None
  }
}
