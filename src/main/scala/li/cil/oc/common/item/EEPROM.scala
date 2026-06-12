package li.cil.oc.common.item

import li.cil.oc.Settings
import li.cil.oc.util.ExtendedItemStack._
import li.cil.oc.util.{BlockPosition, ItemUtils}
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelReader
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.common.extensions.IItemExtension

class EEPROM(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension {
  override def getName(stack: ItemStack): Component = {
    val tag = ItemUtils.getTag(stack)
    if (tag != null && tag.contains(Settings.namespace + "data")) {
      val data = tag.getCompound(Settings.namespace + "data")
      if (data.contains(Settings.namespace + "label")) {
        return Component.literal(data.getString(Settings.namespace + "label"))
      }
    }
    super.getName(stack)
  }

  override def doesSneakBypassUse(stack: ItemStack, world: LevelReader, pos: BlockPos, player: Player): Boolean = true
}
