package li.cil.oc.common.item

import java.util
import com.google.common.base.Strings
import li.cil.oc.Constants
import li.cil.oc.Localization
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.client.{Textures, gui}
import li.cil.oc.common.component
import li.cil.oc.common.blockentity.traits.BaseBlockEntity
import li.cil.oc.common.datacomponents.{OCComponents, TerminalReference}
import li.cil.oc.util.ItemUtils
import li.cil.oc.util.ExtendedItemStack._
import li.cil.oc.util.ExtendedDataComponentHolder._
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.{Properties, TooltipContext}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand
import net.neoforged.neoforge.common.extensions.IItemExtension

class Terminal(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension {
  def hasServer(stack: ItemStack) = stack.has(OCComponents.TERMINAL_REFERENCE)

  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: util.List[Component], flag: TooltipFlag): Unit = {
    super.appendHoverText(stack, context, tooltip, flag)
    for(data <- stack.getComponent(OCComponents.TERMINAL_REFERENCE)) {
      tooltip.add(Component.literal("§8" + data.server.substring(0, 13) + "...§7"))
    }
  }

  //@OnlyIn(Dist.CLIENT)
  //override def getModelLocation(stack: ItemStack) = {
  //  if (hasServer(stack)) Textures.Item.TerminalOn else Textures.Item.TerminalOff
  //}


  override def use(stack: ItemStack, level: Level, player: Player): InteractionResultHolder[ItemStack] = {
    for(TerminalReference(key, server) <- stack.getComponent(OCComponents.TERMINAL_REFERENCE) if !player.isCrouching) {
      if (key.nonEmpty && server.nonEmpty) {
        if (level.isClientSide) {
          if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(server)) {
            component.TerminalServer.loaded.find(server) match {
              case Some(term) if term != null && term.rack != null => term.rack match {
                case rack: BaseBlockEntity with api.internal.Rack => {
                  def inRange = player.isAlive && !rack.isRemoved && player.distanceToSqr(rack.x + 0.5, rack.y + 0.5, rack.z + 0.5) < term.range * term.range
                  if (inRange) {
                    if (term.sidedKeys.contains(key)) showGui(stack, key, term, () => inRange)
                    else player.displayClientMessage(Localization.Terminal.InvalidKey, true)
                  }
                  else player.displayClientMessage(Localization.Terminal.OutOfRange, true)
                }
                case _ => // Eh?
              }
              case _ => player.displayClientMessage(Localization.Terminal.OutOfRange, true)
            }
          }
        }
        player.swing(InteractionHand.MAIN_HAND)
      }
    }
    super.use(stack, level, player)
  }

  @OnlyIn(Dist.CLIENT)
  private def showGui(stack: ItemStack, key: String, term: component.TerminalServer, inRange: () => Boolean): Unit = {
    Minecraft.getInstance.pushGuiLayer(new gui.Screen(term.buffer, true, () => true, () => {
      // Check if someone else bound a term to our server.
      if (stack.getComponent(OCComponents.TERMINAL_REFERENCE).exists(r => r.key != key)) Minecraft.getInstance.popGuiLayer
      // Check whether we're still in range.
      if (!inRange()) Minecraft.getInstance.popGuiLayer
      true
    }))
  }
}
