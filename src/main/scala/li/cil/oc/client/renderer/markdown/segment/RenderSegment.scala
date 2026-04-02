package li.cil.oc.client.renderer.markdown.segment

import com.mojang.blaze3d.vertex.{DefaultVertexFormat, PoseStack, Tesselator, VertexFormat}
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Vector4f
import li.cil.oc.api.manual.ImageRenderer
import li.cil.oc.api.manual.InteractiveImageRenderer
import li.cil.oc.client.renderer.markdown.Document
import li.cil.oc.client.renderer.markdown.MarkupFormat
import net.minecraft.client.gui.Font
import org.lwjgl.opengl.GL11

private[markdown] class RenderSegment(val parent: Segment, val title: String, val imageRenderer: ImageRenderer) extends InteractiveSegment {
  var lastX = 0
  var lastY = 0

  override def tooltip: Option[String] = imageRenderer match {
    case interactive: InteractiveImageRenderer => Option(interactive.getTooltip(title))
    case _ => Option(title)
  }

  override def onMouseClick(mouseX: Int, mouseY: Int): Boolean = imageRenderer match {
    case interactive: InteractiveImageRenderer => interactive.onMouseClick(mouseX - lastX, mouseY - lastY)
    case _ => false
  }

  private def scale(maxWidth: Int) = math.min(1f, maxWidth / imageRenderer.getWidth.toFloat)

  def imageWidth(maxWidth: Int) = math.min(maxWidth, imageRenderer.getWidth)

  def imageHeight(maxWidth: Int) = math.ceil(imageRenderer.getHeight * scale(maxWidth)).toInt + 4

  override def nextY(indent: Int, maxWidth: Int, renderer: Font): Int = imageHeight(maxWidth) + (if (indent > 0) Document.lineHeight(renderer) else 0)

  override def nextX(indent: Int, maxWidth: Int, renderer: Font): Int = 0

  override def render(stack: PoseStack, x: Int, y: Int, indent: Int, maxWidth: Int, renderer: Font, mouseX: Int, mouseY: Int): Option[InteractiveSegment] = {
    val width = imageWidth(maxWidth)
    val height = imageHeight(maxWidth)
    val xOffset = (maxWidth - width) / 2
    val yOffset = 2 + (if (indent > 0) Document.lineHeight(renderer) else 0)
    val s = scale(maxWidth)

    lastX = x + xOffset
    lastY = y + yOffset

    val hovered = checkHovered(mouseX, mouseY, x + xOffset, y + yOffset, width, height)

    stack.pushPose()
    stack.translate(x + xOffset, y + yOffset, 0)
    stack.scale(s, s, s)

    RenderSystem.enableBlend()
    RenderSystem.enableDepthTest()

    if (hovered.isDefined) {
      RenderSystem.setShaderColor(1, 1, 1, 0.15f)
      RenderSystem.disableTexture()

      val matrix = stack.last.pose
      val tesselator = Tesselator.getInstance()
      val buffer = tesselator.getBuilder

      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)
      buffer.vertex(matrix, 0, 0, 0).endVertex()
      buffer.vertex(matrix, 0, imageRenderer.getHeight, 0).endVertex()
      buffer.vertex(matrix, imageRenderer.getWidth, imageRenderer.getHeight, 0).endVertex()
      buffer.vertex(matrix, imageRenderer.getWidth, 0, 0).endVertex()
      tesselator.end()

      RenderSystem.enableTexture()
    }

    RenderSystem.setShaderColor(1, 1, 1, 1)

    imageRenderer.render(stack, mouseX - x, mouseY - y)

    RenderSystem.disableBlend()

    stack.popPose()

    hovered
  }

  override def toString(format: MarkupFormat.Value): String = format match {
    case MarkupFormat.Markdown => s"![$title]($imageRenderer)"
    case MarkupFormat.IGWMod => "(Sorry, images only work in the OpenComputers manual for now.)" // TODO
  }
}
