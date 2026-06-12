package li.cil.oc.common.item

import java.util
import li.cil.oc.Settings
import li.cil.oc.util.{ItemUtils, Tooltip}
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.{Properties, TooltipContext}
import net.minecraft.world.item.ItemStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

import scala.collection.convert.ImplicitConversionsToScala._
import net.minecraft.world.level.Level
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.neoforged.neoforge.common.extensions.IItemExtension

class LinkedCard(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier with IItemExtension {
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: util.List[Component], flag: TooltipFlag): Unit = {
    super.appendHoverText(stack, context, tooltip, flag)
    val tag = ItemUtils.getTag(stack)
    if (tag != null && tag.contains(Settings.namespace + "data")) {
      val data = tag.getCompound(Settings.namespace + "data")
      if (data.contains(Settings.namespace + "tunnel")) {
        val channel = data.getString(Settings.namespace + "tunnel")
        if (channel.length > 13) {
          for (curr <- Tooltip.get(unlocalizedName + "_channel", channel.substring(0, 13) + "...")) {
            tooltip.add(Component.literal(curr).setStyle(Tooltip.DefaultStyle))
          }
        }
        else {
          for (curr <- Tooltip.get(unlocalizedName + "_channel", channel)) {
            tooltip.add(Component.literal(curr).setStyle(Tooltip.DefaultStyle))
          }
        }
      }
    }
  }
}
