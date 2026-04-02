package li.cil.oc.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.common.menu
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import com.mojang.blaze3d.vertex.PoseStack

class Raid(state: menu.Raid, playerInventory: Inventory, name: Component)
  extends DynamicContainerScreen(state, playerInventory, name) {

  override def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int) = {
    RenderSystem.setShaderColor(1, 1, 1, 1) // Required under Linux.
    Textures.bind(Textures.GUI.Raid)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
  }
}
