package li.cil.oc.common.item

import java.util

import li.cil.oc.Settings
import li.cil.oc.util.Tooltip
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.nbt.NbtOps

import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class UpgradeTank(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier {
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, context: Item.TooltipContext, tooltip: util.List[Component], flag: TooltipFlag): Unit = {
    super.appendHoverText(stack, context, tooltip, flag)
    if (stack.hasTag) {
      FluidStack.CODEC.parse(NbtOps.INSTANCE, stack.getTag.getCompound(Settings.namespace + "data")).result().ifPresent { fluidStack =>
        tooltip.add(Component.literal(fluidStack.getDisplayName.getString + ": " + fluidStack.getAmount + "/16000").setStyle(Tooltip.DefaultStyle))
      }
    }
  }
}
