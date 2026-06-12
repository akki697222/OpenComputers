package li.cil.oc.common.item

import java.util
import li.cil.oc.Localization
import li.cil.oc.util.Tooltip
import li.cil.oc.util.ExtendedItemStack._
import li.cil.oc.util.{UpgradeExperience => ExperienceUtil}
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.{Properties, TooltipContext}
import net.minecraft.world.item.ItemStack
import net.neoforged.api.distmarker.{Dist, OnlyIn}
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.common.extensions.IItemExtension

class UpgradeExperience(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier with IItemExtension {
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: util.List[Component], flag: TooltipFlag): Unit = {
    super.appendHoverText(stack, context, tooltip, flag)
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      val nbt = li.cil.oc.integration.opencomputers.Item.dataTag(stack)
      val experience = ExperienceUtil.getExperience(nbt)
      val level = ExperienceUtil.calculateLevelFromExperience(experience)
      val reportedLevel = ExperienceUtil.calculateExperienceLevel(level, experience)
      tooltip.add(Component.literal(Localization.Tooltip.ExperienceLevel(reportedLevel)).setStyle(Tooltip.DefaultStyle))
    }
  }
}
