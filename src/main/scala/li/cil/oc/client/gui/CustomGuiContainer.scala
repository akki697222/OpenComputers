package li.cil.oc.client.gui

import li.cil.oc.client.gui.widget.WidgetContainer
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

import java.util

abstract class CustomGuiContainer[C <: AbstractContainerMenu](val inventoryContainer: C, inv: Inventory, title: Component)
  extends AbstractContainerScreen[C](inventoryContainer, inv, title) with WidgetContainer {

  override def windowX: Int = leftPos

  override def windowY: Int = topPos

  override def windowZ: Float = 0f

  override def isPauseScreen: Boolean = false

  protected def add[T](list: util.List[T], value: Any): Boolean = list.add(value.asInstanceOf[T])

  override def render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks)
    super.render(guiGraphics, mouseX, mouseY, partialTicks)
    this.renderTooltip(guiGraphics, mouseX, mouseY)
  }
}