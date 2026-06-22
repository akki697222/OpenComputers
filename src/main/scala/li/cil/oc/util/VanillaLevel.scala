package li.cil.oc.util

import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/** Vanilla Level calls without ExtendedLevel implicits in scope. */
object VanillaLevel {
  def extinguishFire(level: Level, player: Player, pos: BlockPos, side: Direction): Boolean =
    level.extinguishFire(player, pos, side)
}
