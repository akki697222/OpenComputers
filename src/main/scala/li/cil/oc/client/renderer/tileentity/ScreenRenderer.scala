package li.cil.oc.client.renderer.tileentity

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexConsumer
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.component.{TextBuffer => ComponentTextBuffer}
import li.cil.oc.common.blockentity.Screen
import li.cil.oc.integration.util.Wrench
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionHand
import net.minecraft.core.Direction
import com.mojang.math.Axis
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer => TileEntityRenderer}
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

object ScreenRenderer extends BlockEntityRendererProvider[Screen] {
  override def create(ctx: BlockEntityRendererProvider.Context): ScreenRenderer =
    new ScreenRenderer()
}

class ScreenRenderer extends TileEntityRenderer[Screen] {
  private val maxRenderDistanceSq = Settings.get.maxScreenTextRenderDistance * Settings.get.maxScreenTextRenderDistance
  private val fadeDistanceSq      = Settings.get.screenTextFadeStartDistance * Settings.get.screenTextFadeStartDistance
  private val fadeRatio           = 1.0 / (maxRenderDistanceSq - fadeDistanceSq)

  private var screen: Screen = null

  override def render(
                       screen: Screen,
                       dt: Float,
                       stack: PoseStack,
                       buffer: MultiBufferSource,
                       light: Int,
                       overlay: Int
                     ): Unit = {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    this.screen = screen
    if (!screen.isOrigin) return

    val distance = playerDistanceSq() / math.min(screen.width, screen.height)
    if (distance > maxRenderDistanceSq) return

    val eye_pos   = Minecraft.getInstance.player.getEyePosition(dt)
    val eye_delta = screen.getBlockPos.getY - eye_pos.y

    val screenFacing = screen.facing.getOpposite
    val x            = screen.getBlockPos.getX - eye_pos.x
    val z            = screen.getBlockPos.getZ - eye_pos.z
    if (screenFacing.getStepX * (x + 0.5) + screenFacing.getStepY * (eye_delta + 0.5) + screenFacing.getStepZ * (z + 0.5) < 0) return

    RenderSystem.setShaderColor(1, 1, 1, 1)

    stack.pushPose()
    stack.translate(0.5, 0.5, 0.5)

    RenderState.checkError(getClass.getName + ".render: setup")

    drawOverlay(stack, buffer.getBuffer(RenderTypes.BLOCK_OVERLAY))

    RenderState.checkError(getClass.getName + ".render: overlay")

    val alpha = if (distance > fadeDistanceSq)
      math.max(0, 1 - ((distance - fadeDistanceSq) * fadeRatio).toFloat)
    else 1f

    RenderState.checkError(getClass.getName + ".render: fade")

    if (screen.buffer.isRenderingEnabled) {
      val profiler = Minecraft.getInstance.getProfiler
      profiler.push("opencomputers:screen_text")
      draw(stack, alpha, buffer)
      profiler.pop()
    }

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

  private def transform(stack: PoseStack): Unit = {
    screen.yaw match {
      case Direction.WEST  => stack.mulPose(Axis.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Axis.YP.rotationDegrees(180))
      case Direction.EAST  => stack.mulPose(Axis.YP.rotationDegrees(90))
      case _               => // No yaw.
    }
    screen.pitch match {
      case Direction.DOWN => stack.mulPose(Axis.XP.rotationDegrees(90))
      case Direction.UP   => stack.mulPose(Axis.XP.rotationDegrees(-90))
      case _              => // No pitch.
    }

    stack.translate(-0.5f, -0.5f, 0.5f)
    stack.translate(0, screen.height, 0)
    RenderState.mirrorScale(stack, 1, -1, 1)
  }

  private def isScreen(stack: ItemStack): Boolean = api.Items.get(stack) match {
    case i: ItemInfo => i.block() match {
      case _: li.cil.oc.common.block.Screen => true
      case _                                => false
    }
    case _ => false
  }

  // 1.18.2: IVertexBuilder → VertexConsumer
  private def drawOverlay(matrix: PoseStack, r: VertexConsumer): Unit =
    if (screen.facing == Direction.UP || screen.facing == Direction.DOWN) {
      // 1.18.2: Hand.MAIN_HAND → InteractionHand.MAIN_HAND
      val stack = Minecraft.getInstance.player.getItemInHand(InteractionHand.MAIN_HAND)
      if (!stack.isEmpty) {
        if (Wrench.holdsApplicableWrench(Minecraft.getInstance.player, screen.getBlockPos) || isScreen(stack)) {
          matrix.pushPose()
          transform(matrix)
          matrix.translate(screen.width / 2f - 0.5f, screen.height / 2f - 0.5f, 0.05f)

          val icon = Textures.getSprite(Textures.Block.ScreenUpIndicator)
          r.addVertex(matrix.last.pose, 0, 1, 0).setUv(icon.getU0, icon.getV1)
          r.addVertex(matrix.last.pose, 1, 1, 0).setUv(icon.getU1, icon.getV1)
          r.addVertex(matrix.last.pose, 1, 0, 0).setUv(icon.getU1, icon.getV0)
          r.addVertex(matrix.last.pose, 0, 0, 0).setUv(icon.getU0, icon.getV0)

          matrix.popPose()
        }
      }
    }

  private def draw(stack: PoseStack, alpha: Float, buffer: MultiBufferSource): Unit = {
    RenderState.checkError(getClass.getName + ".draw: entering (aka: wasntme)")

    val sx = screen.width
    val sy = screen.height
    val tw = sx * 16f
    val th = sy * 16f

    transform(stack)

    stack.translate(sx * 2.25f / tw, sy * 2.25f / th, 0)

    val isx = sx - (4.5f / 16)
    val isy = sy - (4.5f / 16)

    val sizeX  = screen.buffer.renderWidth
    val sizeY  = screen.buffer.renderHeight
    val scaleX = isx / sizeX
    val scaleY = isy / sizeY

    if (true) {
      if (scaleX > scaleY) {
        stack.translate(sizeX * 0.5f * (scaleX - scaleY), 0, 0)
        stack.scale(scaleY, scaleY, 1)
      } else {
        stack.translate(0, sizeY * 0.5f * (scaleY - scaleX), 0)
        stack.scale(scaleX, scaleX, 1)
      }
    } else {
      stack.scale(scaleX, scaleY, 1)
    }

    stack.translate(0, 0, 0.01)

    RenderState.checkError(getClass.getName + ".draw: setup")

    screen.buffer match {
      case textBuffer: ComponentTextBuffer => textBuffer.renderText(stack, buffer)
      case _ => screen.buffer.renderText(stack)
    }

    RenderState.checkError(getClass.getName + ".draw: text")
  }

  @OnlyIn(Dist.CLIENT)
  override def shouldRenderOffScreen: Boolean = isOrigin && (width > 1 || height > 1)

  private def playerDistanceSq(): Double = {
    val player = Minecraft.getInstance.player
    val bounds = screen.getRenderBoundingBox

    val px = player.getX
    val py = player.getY
    val pz = player.getZ

    val ex = bounds.maxX - bounds.minX
    val ey = bounds.maxY - bounds.minY
    val ez = bounds.maxZ - bounds.minZ
    val cx = bounds.minX + ex * 0.5
    val cy = bounds.minY + ey * 0.5
    val cz = bounds.minZ + ez * 0.5
    val dx = px - cx
    val dy = py - cy
    val dz = pz - cz

    (if (dx < -ex) { val d = dx + ex; d * d }
    else if (dx > ex) { val d = dx - ex; d * d }
    else 0.0) +
      (if (dy < -ey) { val d = dy + ey; d * d }
      else if (dy > ey) { val d = dy - ey; d * d }
      else 0.0) +
      (if (dz < -ez) { val d = dz + ez; d * d }
      else if (dz > ez) { val d = dz - ez; d * d }
      else 0.0)
  }
}
