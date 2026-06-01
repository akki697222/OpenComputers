package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import com.google.common.base.Strings
import li.cil.oc.Settings
import li.cil.oc.client.KeyBindings
import li.cil.oc.client.Textures
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.common.blockentity
import li.cil.oc.util.Color
import li.cil.oc.util.ExtendedAABB
import li.cil.oc.util.ExtendedAABB._
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.client.renderer.RenderType
import net.minecraftforge.client.model.data.{ModelData, ModelProperty}

import scala.collection.JavaConverters.bufferAsJavaList
import scala.jdk.CollectionConverters._
import scala.collection.mutable

object PrintModel extends SmartBlockModelBase {
  val PRINT_PROPERTY = new ModelProperty[blockentity.Print]()

  override def getOverrides: ItemOverrides = ItemOverride

  override def getQuads(state: BlockState, side: Direction, rand: RandomSource, data: ModelData, renderType: RenderType): util.List[BakedQuad] =
    Option(data.get(PRINT_PROPERTY)) match {
      case Some(t) =>
        val faces = mutable.ArrayBuffer.empty[BakedQuad]
        for (shape <- t.shapes if !Strings.isNullOrEmpty(shape.texture)) {
          val bounds  = shape.bounds.rotateTowards(t.facing)
          val texture = resolveTexture(shape.texture)
          faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), shape.tint.getOrElse(White))
        }
        faces.asJava
      case _ => super.getQuads(state, side, rand)
    }

  private def resolveTexture(name: String): TextureAtlasSprite = {
    def isMissing(s: TextureAtlasSprite) =
      s.contents.name == MissingTextureAtlasSprite.getLocation

    def tryGet(loc: ResourceLocation): Option[TextureAtlasSprite] = {
      val s = Textures.getSprite(loc)
      if (!isMissing(s)) Some(s) else None
    }

    Option(ResourceLocation.tryParse(name)).flatMap(tryGet)
      .orElse(tryGet(ResourceLocation.withDefaultNamespace("block/" + name)))
      .orElse(tryGet(ResourceLocation.fromNamespaceAndPath(Settings.resourceDomain, "block/" + name)))
      .getOrElse(Textures.getSprite(MissingTextureAtlasSprite.getLocation))
  }

  class ItemModel(val stack: ItemStack) extends SmartBlockModelBase {
    val data = new PrintData(stack)

    override def getQuads(state: BlockState, side: Direction, rand: RandomSource): util.List[BakedQuad] = {
      val faces = mutable.ArrayBuffer.empty[BakedQuad]
      val shapes =
        if (data.hasActiveState && KeyBindings.showExtendedTooltips) data.stateOn
        else data.stateOff
      for (shape <- shapes) {
        val bounds  = shape.bounds
        val texture = resolveTexture(shape.texture)
        faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), shape.tint.getOrElse(White))
      }
      if (shapes.isEmpty) {
        val bounds  = ExtendedAABB.unitBounds
        val texture = resolveTexture(Settings.resourceDomain + ":block/white")
        faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), Color.rgbValues(DyeColor.LIME))
      }
      bufferAsJavaList(faces)
    }
  }

  object ItemOverride extends ItemOverrides {
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, seed: Int): BakedModel =
      new ItemModel(stack)
  }
}