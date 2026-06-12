package li.cil.oc.integration.cofh.foundation

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object EventHandlerFoundation {
  def useWrench(player: Player, pos: BlockPos, changeDurability: Boolean): Boolean = {
    false
  }

  def isWrench(stack: ItemStack): Boolean = false
}
