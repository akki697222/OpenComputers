package li.cil.oc.client.renderer.tileentity

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Vector3f
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity.Microcontroller
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation

/**
 * 1.18.2 Mojmap 完全移植版.
 * MatrixStack -> PoseStack, IVertexBuilder -> VertexConsumer へ置換。
 */
class MicrocontrollerRenderer(ctx: BlockEntityRendererProvider.Context) extends BlockEntityRenderer[Microcontroller] {

  override def render(mcu: Microcontroller, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int): Unit = {
    RenderState.checkError(getClass.getName + ".render: entering")

    // 1.18.2 では color4f が廃止されているため setShaderColor を使用
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

    stack.pushPose()

    // ブロックの中心に移動
    stack.translate(0.5, 0.5, 0.5)

    // マイクロコントローラーの向きに合わせて回転
    mcu.yaw match {
      case Direction.WEST => stack.mulPose(Vector3f.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Vector3f.YP.rotationDegrees(180))
      case Direction.EAST => stack.mulPose(Vector3f.YP.rotationDegrees(90))
      case _ => // No yaw.
    }

    // 正面パネルの少し前にオフセット
    stack.translate(-0.5, 0.5, 0.505)
    RenderState.mirrorScale(stack, 1.0F, -1.0F, 1.0F)

    val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

    // ベースとなるライト層の描画
    renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontLight, r)

    // 状態に応じた追加のオーバーレイ描画
    if (mcu.isRunning) {
      renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontOn, r)
    } else if (mcu.hasErrored && RenderUtil.shouldShowErrorLight(mcu.hashCode)) {
      renderFrontOverlay(stack, Textures.Block.MicrocontrollerFrontError, r)
    }

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

  /**
   * 正面オーバーレイの描画ヘルパー.
   * 引数を 1.18.2 の PoseStack と VertexConsumer に変更。
   */
  private def renderFrontOverlay(stack: PoseStack, texture: ResourceLocation, r: VertexConsumer): Unit = {
    val icon = Textures.getSprite(texture)
    val matrix = stack.last.pose

    r.vertex(matrix, 0, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
    r.vertex(matrix, 1, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
    r.vertex(matrix, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
    r.vertex(matrix, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()
  }
}