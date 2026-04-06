package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import li.cil.oc.client.Textures
import li.cil.oc.common.block.property.PropertyCableConnection
import li.cil.oc.common.blockentity
import li.cil.oc.util.Color
import li.cil.oc.util.ExtendedLevel._
import li.cil.oc.util.ItemColorizer
import net.minecraft.world.level.block.state.BlockState                    // 1.18.2: net.minecraft.block → net.minecraft.world.level.block.state
import net.minecraft.client.renderer.block.model.BakedQuad                 // 1.18.2: net.minecraft.client.renderer.model → block.model
import net.minecraft.client.resources.model.BakedModel                     // 1.18.2: IBakedModel → BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides              // 1.18.2: ItemOverrideList → ItemOverrides
import net.minecraft.client.multiplayer.ClientLevel                         // 1.18.2: ClientWorld → ClientLevel
import net.minecraft.world.entity.LivingEntity                              // 1.18.2: net.minecraft.entity → net.minecraft.world.entity
import net.minecraft.world.item.DyeColor                                    // 1.18.2: net.minecraft.item → net.minecraft.world.item
import net.minecraft.world.item.ItemStack                                   // 1.18.2: net.minecraft.item → net.minecraft.world.item
import net.minecraft.core.Direction                                         // 1.18.2: net.minecraft.util.Direction → net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3                                        // 1.18.2: Vector3d → Vec3
import net.minecraft.world.level.block.state.properties.Property           // キャスト用
import net.minecraftforge.client.model.data.IModelData

import scala.collection.JavaConverters.bufferAsJavaList
import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.mutable

object CableModel extends SmartBlockModelBase {
  override def getOverrides: ItemOverrides = ItemOverride

  // ---------------------------------------------------------------------------
  // EnumProperty[Shape] は実行時に Comparable を実装しているが、Scala コンパイラは
  // Property[T where T <: Comparable[T]] という上限境界を静的に証明できない。
  // asInstanceOf でキャストして getValue に渡す。
  // ---------------------------------------------------------------------------
  @inline private def getShape(state: BlockState, d: Direction): PropertyCableConnection.Shape = {
    val prop = PropertyCableConnection.BY_DIRECTION.get(d).asInstanceOf[Property[_]]
    state.getValue(prop).asInstanceOf[PropertyCableConnection.Shape]
  }

  override def getQuads(state: BlockState, side: Direction, rand: util.Random, data: IModelData): util.List[BakedQuad] = {
    data match {
      case cable: blockentity.Cable if side == null =>
        val color = cable.getColor
        val faces = mutable.ArrayBuffer.empty[BakedQuad]

        faces ++= bakeQuads(Middle, cableTexture, color)
        val directions = Direction.values
        val numConnected = directions.count(d => getShape(state, d) != PropertyCableConnection.Shape.NONE)
        for (side <- directions) {
          val shape      = getShape(state, side)
          val connected  = shape != PropertyCableConnection.Shape.NONE
          val isCableOnSide = shape == PropertyCableConnection.Shape.CABLE
          val (plug, shortBody, longBody) = Connected(side.get3DDataValue)
          if (connected) {
            if (isCableOnSide) faces ++= bakeQuads(longBody, cableTexture, color)
            else {
              faces ++= bakeQuads(shortBody, cableTexture, color)
              faces ++= bakeQuads(plug, cableCapTexture, None)
            }
          } else {
            val otherConn = getShape(state, side.getOpposite) != PropertyCableConnection.Shape.NONE
            if ((otherConn && numConnected == 1) || numConnected == 0)
              faces ++= bakeQuads(Disconnected(side.get3DDataValue), cableCapTexture, None)
          }
        }
        bufferAsJavaList(faces)
      case _ => super.getQuads(state, side, rand)
    }
  }

  // 1.18.2: new Vector3d(...) → new Vec3(...)
  protected final val Middle = makeBox(new Vec3(6 / 16f, 6 / 16f, 6 / 16f), new Vec3(10 / 16f, 10 / 16f, 10 / 16f))

  protected final val Connected = Array(
    (makeBox(new Vec3(5 / 16f, 0 / 16f, 5 / 16f),  new Vec3(11 / 16f, 1 / 16f, 11 / 16f)),
      makeBox(new Vec3(6 / 16f, 1 / 16f, 6 / 16f),  new Vec3(10 / 16f, 6 / 16f, 10 / 16f)),
      makeBox(new Vec3(6 / 16f, 0 / 16f, 6 / 16f),  new Vec3(10 / 16f, 6 / 16f, 10 / 16f))),
    (makeBox(new Vec3(5 / 16f, 15 / 16f, 5 / 16f), new Vec3(11 / 16f, 16 / 16f, 11 / 16f)),
      makeBox(new Vec3(6 / 16f, 10 / 16f, 6 / 16f), new Vec3(10 / 16f, 15 / 16f, 10 / 16f)),
      makeBox(new Vec3(6 / 16f, 10 / 16f, 6 / 16f), new Vec3(10 / 16f, 16 / 16f, 10 / 16f))),
    (makeBox(new Vec3(5 / 16f, 5 / 16f, 0 / 16f),  new Vec3(11 / 16f, 11 / 16f, 1 / 16f)),
      makeBox(new Vec3(6 / 16f, 6 / 16f, 1 / 16f),  new Vec3(10 / 16f, 10 / 16f, 6 / 16f)),
      makeBox(new Vec3(6 / 16f, 6 / 16f, 0 / 16f),  new Vec3(10 / 16f, 10 / 16f, 6 / 16f))),
    (makeBox(new Vec3(5 / 16f, 5 / 16f, 15 / 16f), new Vec3(11 / 16f, 11 / 16f, 16 / 16f)),
      makeBox(new Vec3(6 / 16f, 6 / 16f, 10 / 16f), new Vec3(10 / 16f, 10 / 16f, 15 / 16f)),
      makeBox(new Vec3(6 / 16f, 6 / 16f, 10 / 16f), new Vec3(10 / 16f, 10 / 16f, 16 / 16f))),
    (makeBox(new Vec3(0 / 16f, 5 / 16f, 5 / 16f),  new Vec3(1 / 16f, 11 / 16f, 11 / 16f)),
      makeBox(new Vec3(1 / 16f, 6 / 16f, 6 / 16f),  new Vec3(6 / 16f, 10 / 16f, 10 / 16f)),
      makeBox(new Vec3(0 / 16f, 6 / 16f, 6 / 16f),  new Vec3(6 / 16f, 10 / 16f, 10 / 16f))),
    (makeBox(new Vec3(15 / 16f, 5 / 16f, 5 / 16f), new Vec3(16 / 16f, 11 / 16f, 11 / 16f)),
      makeBox(new Vec3(10 / 16f, 6 / 16f, 6 / 16f), new Vec3(15 / 16f, 10 / 16f, 10 / 16f)),
      makeBox(new Vec3(10 / 16f, 6 / 16f, 6 / 16f), new Vec3(16 / 16f, 10 / 16f, 10 / 16f)))
  )

  protected final val Disconnected = Array(
    makeBox(new Vec3(6 / 16f, 5 / 16f,  6 / 16f), new Vec3(10 / 16f, 6 / 16f,  10 / 16f)),
    makeBox(new Vec3(6 / 16f, 10 / 16f, 6 / 16f), new Vec3(10 / 16f, 11 / 16f, 10 / 16f)),
    makeBox(new Vec3(6 / 16f, 6 / 16f,  5 / 16f), new Vec3(10 / 16f, 10 / 16f, 6 / 16f)),
    makeBox(new Vec3(6 / 16f, 6 / 16f, 10 / 16f), new Vec3(10 / 16f, 10 / 16f, 11 / 16f)),
    makeBox(new Vec3(5 / 16f, 6 / 16f,  6 / 16f), new Vec3(6 / 16f,  10 / 16f, 10 / 16f)),
    makeBox(new Vec3(10 / 16f, 6 / 16f, 6 / 16f), new Vec3(11 / 16f, 10 / 16f, 10 / 16f))
  )

  protected def cableTexture    = Array.fill(6)(Textures.getSprite(Textures.Block.Cable))
  protected def cableCapTexture = Array.fill(6)(Textures.getSprite(Textures.Block.CableCap))

  object ItemOverride extends ItemOverrides {
    class ItemModel(val stack: ItemStack) extends SmartBlockModelBase {
      override def getQuads(state: BlockState, side: Direction, rand: util.Random): util.List[BakedQuad] = {
        val faces = mutable.ArrayBuffer.empty[BakedQuad]
        val color = if (ItemColorizer.hasColor(stack)) ItemColorizer.getColor(stack) else Color.rgbValues(DyeColor.LIGHT_GRAY)
        faces ++= bakeQuads(Middle, cableTexture, Some(color))
        faces ++= bakeQuads(Connected(0)._2, cableTexture, Some(color))
        faces ++= bakeQuads(Connected(1)._2, cableTexture, Some(color))
        faces ++= bakeQuads(Connected(0)._1, cableCapTexture, None)
        faces ++= bakeQuads(Connected(1)._1, cableCapTexture, None)
        bufferAsJavaList(faces)
      }
    }

    // 1.18.2: resolve に seed: Int 引数が追加された
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, seed: Int): BakedModel =
      new ItemModel(stack)
  }
}