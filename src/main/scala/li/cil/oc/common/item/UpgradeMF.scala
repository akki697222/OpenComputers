package li.cil.oc.common.item

import java.util
import li.cil.oc.Localization
import li.cil.oc.Settings
import li.cil.oc.common.datacomponents.{MFCoords, OCComponents}
import li.cil.oc.util.Tooltip
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.network.chat.Component
import net.minecraft.nbt.CompoundTag
import net.neoforged.neoforge.common.extensions.IItemExtension

class UpgradeMF(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier with IItemExtension {
  override def onItemUseFirst(stack: ItemStack, player: Player, level: Level, pos: BlockPos, side: Direction, hitX: Float, hitY: Float, hitZ: Float, hand: InteractionHand): InteractionResult = {
    if (!player.level.isClientSide && player.isCrouching) {
      stack.set(OCComponents.MF_COORD, MFCoords(level.dimension.location, pos, side))
      return InteractionResult.sidedSuccess(player.level.isClientSide)
    }
    super.onItemUseFirst(stack, player, level, pos, side, hitX, hitY, hitZ, hand)
  }

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[Component]): Unit = {
    tooltip.add(Component.literal(Localization.Tooltip.MFULinked(stack.has(OCComponents.MF_COORD))).setStyle(Tooltip.DefaultStyle))
  }
}
