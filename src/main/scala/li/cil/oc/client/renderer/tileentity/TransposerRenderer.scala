package li.cil.oc.client.renderer.tileentity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer => TileEntityRenderer}
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

// 1.18.2: BlockEntityRendererProvider[T] に変更
object TransposerRenderer extends BlockEntityRendererProvider[blockentity.Transposer] {
  override def create(ctx: BlockEntityRendererProvider.Context): TransposerRenderer =
    new TransposerRenderer()
}

// 1.18.2: コンストラクタ引数なし
class TransposerRenderer extends TileEntityRenderer[blockentity.Transposer] {
  override def render(
                       transposer: blockentity.Transposer,
                       dt: Float,
                       stack: PoseStack, // 1.18.2: MatrixStack → PoseStack
                       buffer: MultiBufferSource, // 1.18.2: IRenderTypeBuffer → MultiBufferSource
                       light: Int,
                       overlay: Int
                     ): Unit = {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShaderColor(1, 1, 1, 1) // 1.18.2: color4f → setShaderColor

    val activity = math.max(0, 1 - (System.currentTimeMillis() - transposer.lastOperation) / 1000.0f)
    if (activity > 0) {
      stack.pushPose()

      stack.translate(0.5, 0.5, 0.5)
      RenderState.mirrorScale(stack, 1.0025f, -1.0025f, 1.0025f)
      stack.translate(-0.5f, -0.5f, -0.5f)

      // 1.18.2: BLOCK_OVERLAY_COLOR は透明度付きカラーをサポートするバッファ
      val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY_COLOR)

      val icon = Textures.getSprite(Textures.Block.TransposerOn)

      // 上面
      r.vertex(stack.last.pose, 0, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()

      // 下面
      r.vertex(stack.last.pose, 0, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()

      // 南面 (Z-)
      r.vertex(stack.last.pose, 1, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()

      // 北面 (Z+)
      r.vertex(stack.last.pose, 0, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()

      // 西面 (X-)
      r.vertex(stack.last.pose, 0, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()

      // 東面 (X+)
      r.vertex(stack.last.pose, 1, 1, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).color(1f, 1f, 1f, activity).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).color(1f, 1f, 1f, activity).uv(icon.getU0, icon.getV0).endVertex()

      stack.popPose()
    }

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}