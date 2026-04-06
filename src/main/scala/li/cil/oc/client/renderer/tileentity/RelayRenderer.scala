package li.cil.oc.client.renderer.tileentity

import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.blockentity
import li.cil.oc.util.RenderState
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer => TileEntityRenderer}
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

// 1.18.2: BlockEntityRendererProvider[T] に変更
object RelayRenderer extends BlockEntityRendererProvider[blockentity.Relay] {
  override def create(ctx: BlockEntityRendererProvider.Context): RelayRenderer =
    new RelayRenderer()
}

// 1.18.2: コンストラクタ引数なし
class RelayRenderer extends TileEntityRenderer[blockentity.Relay] {
  override def render(
                       switch: blockentity.Relay,
                       dt: Float,
                       stack: PoseStack, // 1.18.2: MatrixStack → PoseStack
                       buffer: MultiBufferSource, // 1.18.2: IRenderTypeBuffer → MultiBufferSource
                       light: Int,
                       overlay: Int
                     ): Unit = {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    RenderSystem.setShaderColor(1, 1, 1, 1) // 1.18.2: color4f → setShaderColor

    val activity = math.max(0, 1 - (System.currentTimeMillis() - switch.lastMessage) / 1000.0)
    if (activity > 0) {
      stack.pushPose()

      stack.translate(0.5, 0.5, 0.5)
      RenderState.mirrorScale(stack, 1.0025f, -1.0025f, 1.0025f)
      stack.translate(-0.5f, -0.5f, -0.5f)

      val r = buffer.getBuffer(RenderTypes.BLOCK_OVERLAY)

      val icon = Textures.getSprite(Textures.Block.SwitchSideOn)
      r.vertex(stack.last.pose, 1, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 0, 1, 1).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 1).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 1, 1, 1).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 1, 0, 1).uv(icon.getU0, icon.getV0).endVertex()

      r.vertex(stack.last.pose, 0, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 1, 1).uv(icon.getU1, icon.getV1).endVertex()
      r.vertex(stack.last.pose, 0, 0, 1).uv(icon.getU1, icon.getV0).endVertex()
      r.vertex(stack.last.pose, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()

      stack.popPose()
    }

    RenderState.checkError(getClass.getName + ".render: leaving")
  }
}