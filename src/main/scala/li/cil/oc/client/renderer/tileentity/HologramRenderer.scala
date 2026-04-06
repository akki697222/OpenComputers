package li.cil.oc.client.renderer.tileentity

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.{PoseStack, VertexConsumer}
import com.mojang.math.{Matrix4f, Vector3f}
import li.cil.oc.Settings
import li.cil.oc.client.Textures
import li.cil.oc.common.blockentity.Hologram
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{MultiBufferSource, RenderType}
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider}
import net.minecraft.core.Direction

import scala.util.Random

object HologramRenderer extends BlockEntityRendererProvider[Hologram] {
  override def create(ctx: BlockEntityRendererProvider.Context): BlockEntityRenderer[Hologram] =
    new HologramRenderer()
}

class HologramRenderer extends BlockEntityRenderer[Hologram] {

  private val random = new Random()

  override def render(
                       hologram: Hologram,
                       partialTick: Float,
                       stack: PoseStack,
                       buffer: MultiBufferSource,
                       packedLight: Int,
                       packedOverlay: Int
                     ): Unit = {

    if (!hologram.hasPower) return

    RenderState.checkError(getClass.getName + ".render: entering")

    RenderState.makeItBlend()
    RenderSystem.blendFuncSeparate(
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE,
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ZERO
    )

    val pos = hologram.getBlockPos
    val relPos = Minecraft.getInstance.player.getEyePosition(partialTick)
      .subtract(pos.getX + 0.5, pos.getY + 0.5, pos.getZ + 0.5)

    val distSq = relPos.dot(relPos)
    val fadeDistSq = hologram.getFadeStartDistanceSquared
    val maxDistSq = hologram.getViewDistance * hologram.getViewDistance

    val alpha =
      0.75f * (if (distSq > fadeDistSq)
        math.max(0, 1 - ((distSq - fadeDistSq) / (maxDistSq - fadeDistSq)).toFloat)
      else 1f)

    RenderSystem.setShaderColor(1f, 1f, 1f, alpha)

    stack.pushPose()

    // 位置合わせ
    stack.translate(0.5, 0.5, 0.5)

    // 向き
    hologram.yaw match {
      case Direction.WEST  => stack.mulPose(Vector3f.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Vector3f.YP.rotationDegrees(180))
      case Direction.EAST  => stack.mulPose(Vector3f.YP.rotationDegrees(90))
      case _ =>
    }

    hologram.pitch match {
      case Direction.DOWN => stack.mulPose(Vector3f.XP.rotationDegrees(90))
      case Direction.UP   => stack.mulPose(Vector3f.XP.rotationDegrees(-90))
      case _ =>
    }

    // 自由回転
    stack.mulPose(
      new Vector3f(hologram.rotationX, hologram.rotationY, hologram.rotationZ)
        .rotationDegrees(hologram.rotationAngle)
    )

    stack.mulPose(
      new Vector3f(hologram.rotationSpeedX, hologram.rotationSpeedY, hologram.rotationSpeedZ)
        .rotationDegrees(
          hologram.rotationSpeed *
            (hologram.getLevel.getGameTime % (360 * 20 - 1) + partialTick) / 20f
        )
    )

    stack.scale(1.001f, 1.001f, 1.001f)

    stack.translate(
      (hologram.translation.x * hologram.width / 16.0 - 1.5) * hologram.scale,
      hologram.translation.y * hologram.height / 16.0 * hologram.scale,
      (hologram.translation.z * hologram.width / 16.0 - 1.5) * hologram.scale
    )

    // フリッカー
    if (Settings.get.hologramFlickerFrequency > 0 &&
      random.nextDouble() < Settings.get.hologramFlickerFrequency) {
      stack.scale(
        Math.max(1f + (random.nextGaussian() * 0.01).toFloat, 0.001f),
        Math.max(1f + (random.nextGaussian() * 0.001).toFloat, 0.001f),
        Math.max(1f + (random.nextGaussian() * 0.01).toFloat, 0.001f)
      )
    }

    RenderState.mirrorScale(
      stack,
      hologram.scale.toFloat / 16f,
      hologram.scale.toFloat / 16f,
      hologram.scale.toFloat / 16f
    )

    stack.translate(-0.5, -0.5, -0.5)

    val renderType = RenderType.entityTranslucent(Textures.Model.HologramEffect)

    val vb = buffer.getBuffer(renderType)
    val matrix = stack.last.pose

    renderHologramGeometry(hologram, vb, matrix)

    stack.popPose()

    RenderState.disableBlend()
    RenderSystem.defaultBlendFunc()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

  // ======== 完全置換ジオメトリ生成 ========

  private def renderHologramGeometry(
                                      hologram: Hologram,
                                      vb: VertexConsumer,
                                      matrix: Matrix4f
                                    ): Unit = {

    def value(x: Int, y: Int, z: Int): Int =
      if (x >= 0 && y >= 0 && z >= 0 &&
        x < hologram.width &&
        y < hologram.height &&
        z < hologram.width)
        hologram.getColor(x, y, z)
      else 0

    def solid(x: Int, y: Int, z: Int): Boolean =
      value(x, y, z) != 0

    def quad(
              x: Float, y: Float, z: Float,
              r: Int, g: Int, b: Int,
              u: Float, v: Float
            ): Unit =
      vb.vertex(matrix, x, y, z)
        .color(r, g, b, 255)
        .uv(u, v)
        .endVertex()

    for {
      x <- 0 until hologram.width
      y <- 0 until hologram.height
      z <- 0 until hologram.width
      if solid(x, y, z)
    } {
      val c = hologram.colors(value(x, y, z) - 1)
      val r =  c        & 0xFF
      val g = (c >> 8) & 0xFF
      val b = (c >>16) & 0xFF

      if (!solid(x, y, z + 1)) {
        quad(x+1,y+1,z+1,r,g,b,0,0)
        quad(x,y+1,z+1,r,g,b,1,0)
        quad(x,y,z+1,r,g,b,1,1)
        quad(x+1,y,z+1,r,g,b,0,1)
      }
      if (!solid(x, y, z - 1)) {
        quad(x+1,y,z,r,g,b,0,0)
        quad(x,y,z,r,g,b,1,0)
        quad(x,y+1,z,r,g,b,1,1)
        quad(x+1,y+1,z,r,g,b,0,1)
      }
      if (!solid(x + 1, y, z)) {
        quad(x+1,y+1,z+1,r,g,b,1,0)
        quad(x+1,y,z+1,r,g,b,1,1)
        quad(x+1,y,z,r,g,b,0,1)
        quad(x+1,y+1,z,r,g,b,0,0)
      }
      if (!solid(x - 1, y, z)) {
        quad(x,y,z+1,r,g,b,1,0)
        quad(x,y+1,z+1,r,g,b,1,1)
        quad(x,y+1,z,r,g,b,0,1)
        quad(x,y,z,r,g,b,0,0)
      }
      if (!solid(x, y + 1, z)) {
        quad(x+1,y+1,z,r,g,b,0,0)
        quad(x,y+1,z,r,g,b,1,0)
        quad(x,y+1,z+1,r,g,b,1,1)
        quad(x+1,y+1,z+1,r,g,b,0,1)
      }
      if (!solid(x, y - 1, z)) {
        quad(x+1,y,z+1,r,g,b,0,0)
        quad(x,y,z+1,r,g,b,1,0)
        quad(x,y,z,r,g,b,1,1)
        quad(x+1,y,z,r,g,b,0,1)
      }
    }
  }
}