package li.cil.oc.common.block.traits

import java.util
import li.cil.oc.common.block.SimpleBlock
import li.cil.oc.util.Tooltip
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.level.BlockGetter

import scala.collection.convert.ImplicitConversionsToScala._

trait PowerAcceptor extends SimpleBlock {
  def energyThroughput: Double

  // ----------------------------------------------------------------------- //

  override protected def tooltipTail(stack: ItemStack, context: TooltipContext, tooltip: util.List[Component], advanced: TooltipFlag): Unit = {
    super.tooltipTail(stack, context, tooltip, advanced)
    for (curr <- Tooltip.extended("poweracceptor", energyThroughput.toInt)) {
      tooltip.add(Component.literal(curr).setStyle(Tooltip.DefaultStyle))
    }
  }
}
