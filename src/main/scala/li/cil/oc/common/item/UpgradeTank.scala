package li.cil.oc.common.item

import java.util
import li.cil.oc.Settings
import li.cil.oc.util.Tooltip
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.{Properties, TooltipContext}
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.neoforged.neoforge.common.extensions.IItemExtension

class UpgradeTank(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier with IItemExtension {
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: util.List[Component], flag: TooltipFlag): Unit = {
    super.appendHoverText(stack, context, tooltip, flag)
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      FluidStack.parse(context.level().registryAccess(), stack.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(Settings.namespace + "data")).get() match {
        case stack: FluidStack =>
          tooltip.add(Component.literal(stack.getDisplayName.getString + ": " + stack.getAmount + "/16000").setStyle(Tooltip.DefaultStyle))
        case _ =>
      }
    }
  }
}
