package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.{DefaultVertexFormat, PoseStack, Tesselator, VertexFormat}
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.common
import li.cil.oc.common.menu.ComponentSlot
import li.cil.oc.common.menu.AbstractMenu
import li.cil.oc.integration.util.ItemSearch
import li.cil.oc.util.RenderState
import li.cil.oc.util.StackOption
import li.cil.oc.util.StackOption._
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}

import net.minecraft.network.chat.Component

abstract class DynamicContainerScreen[C <: AbstractContainerMenu](container: C, inv: Inventory, title: Component)
  extends CustomContainerScreen(container, inv, title) {

  protected var hoveredStackNEI: StackOption = EmptyStack

  override protected def init(): Unit = {
    super.init()
    // imageHeight is set in the body of the extending class, so it's not available in ours.
    inventoryLabelY = imageHeight - 96 + 2
  }

  protected def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {}

  override protected def renderLabels(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    super.renderLabels(stack, mouseX, mouseY)
    RenderState.pushAttrib()

    drawSecondaryForegroundLayer(stack, mouseX, mouseY)

    for (slot <- 0 until menu.slots.size()) {
      drawSlotHighlight(stack, menu.getSlot(slot))
    }

    RenderState.popAttrib()
  }

  protected def drawSecondaryBackgroundLayer(stack: PoseStack): Unit = {}

  override protected def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    Textures.bind(Textures.GUI.Background)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    drawSecondaryBackgroundLayer(stack)

    RenderState.makeItBlend()
    RenderSystem.setShader(() => GameRenderer.getPositionColorShader)

    drawInventorySlots(stack)
  }

  protected def drawInventorySlots(stack: PoseStack): Unit = {
    stack.pushPose()
    stack.translate(leftPos, topPos, 0)
    RenderSystem.disableDepthTest()
    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    for (slot <- 0 until menu.slots.size()) {
      drawSlotInventory(stack, menu.getSlot(slot))
    }
    RenderSystem.enableDepthTest()
    stack.popPose()
    RenderState.makeItBlend()
  }

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    hoveredStackNEI = ItemSearch.hoveredStack(this, mouseX, mouseY)

    super.render(stack, mouseX, mouseY, dt)
  }

  protected def drawSlotInventory(stack: PoseStack, slot: Slot): Unit = {
    RenderSystem.enableBlend()
    slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None =>
        if (!slot.hasItem && slot.x >= 0 && slot.y >= 0 && component.tierIcon != null) {
          drawDisabledSlot(stack, component)
        }
      case _ =>
        stack.pushPose()
        stack.translate(0, 0, 1)
        if (!isInPlayerInventory(slot)) {
          drawSlotBackground(stack, slot.x - 1, slot.y - 1)
        }
        slot match {
          case component: ComponentSlot if !slot.hasItem =>
            if (component.tierIcon != null) {
              Textures.bind(component.tierIcon)
              GuiComponent.blit(stack, slot.x, slot.y, 0, 0, 0, 16, 16, 16, 16)
            }
            if (component.hasBackground) {
              Textures.bind(component.getBackgroundLocation)
              GuiComponent.blit(stack, slot.x, slot.y, 0, 0, 0, 16, 16, 16, 16)
            }
          case _ =>
        }
        stack.popPose()
    }
    RenderSystem.disableBlend()
  }

  protected def drawSlotHighlight(stack: PoseStack, slot: Slot): Unit = {
    if (minecraft.player.containerMenu.getCarried.isEmpty) slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None => // Ignore.
      case _ =>
        val currentIsInPlayerInventory = isInPlayerInventory(slot)
        val drawHighlight = hoveredSlot match {
          case hovered: Slot =>
            val hoveredIsInPlayerInventory = isInPlayerInventory(hovered)
            (currentIsInPlayerInventory != hoveredIsInPlayerInventory) &&
              ((currentIsInPlayerInventory && slot.hasItem && isSelectiveSlot(hovered) && hovered.mayPlace(slot.getItem)) ||
                (hoveredIsInPlayerInventory && hovered.hasItem && isSelectiveSlot(slot) && slot.mayPlace(hovered.getItem)))
          case _ => hoveredStackNEI match {
            case SomeStack(stack) => !currentIsInPlayerInventory && isSelectiveSlot(slot) && slot.mayPlace(stack)
            case _ => false
          }
        }
        if (drawHighlight) {
          stack.pushPose()
          stack.translate(0, 0, 100)
          fillGradient(stack,
            slot.x, slot.y,
            slot.x + 16, slot.y + 16,
            0x80FFFFFF, 0x80FFFFFF)
          stack.popPose()
        }
    }
  }

  private def isSelectiveSlot(slot: Slot) = slot match {
    case component: ComponentSlot => component.slot != common.Slot.Any && component.slot != common.Slot.Tool
    case _ => false
  }

  protected def drawDisabledSlot(stack: PoseStack, slot: ComponentSlot): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    Textures.bind(slot.tierIcon)
    GuiComponent.blit(stack, slot.x, slot.y, 0, 0, 0, 16, 16, 16, 16)
  }

  protected def drawSlotBackground(stack: PoseStack, x: Int, y: Int): Unit = {
    RenderSystem.setShaderColor(1, 1, 1, 1)
    Textures.bind(Textures.GUI.Slot)
    val t = Tesselator.getInstance
    val r = t.getBuilder
    r.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    r.vertex(stack.last.pose, x, y + 18, 1).uv(0, 1).endVertex()
    r.vertex(stack.last.pose, x + 18, y + 18, 1).uv(1, 1).endVertex()
    r.vertex(stack.last.pose, x + 18, y, 1).uv(1, 0).endVertex()
    r.vertex(stack.last.pose, x, y, 1).uv(0, 0).endVertex()
    t.end()
  }

  private def isInPlayerInventory(slot: Slot) = container match {
    case player: AbstractMenu => slot.container == player.playerInventory
    case _ => false
  }
}
