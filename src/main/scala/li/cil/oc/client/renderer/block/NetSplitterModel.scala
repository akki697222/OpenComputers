package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import li.cil.oc.OpenComputers
import li.cil.oc.client.Textures
import li.cil.oc.common.block
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.common.blockentity
import net.minecraft.world.level.block.state.BlockState                    // 1.18.2
import net.minecraft.client.multiplayer.ClientLevel                         // 1.18.2: ClientWorld → ClientLevel
import net.minecraft.client.renderer.block.model.BakedQuad                 // 1.18.2
import net.minecraft.client.resources.model.BakedModel                     // 1.18.2: IBakedModel → BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides              // 1.18.2: ItemOverrideList → ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlas                   // 1.18.2: AtlasTexture → TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.world.entity.LivingEntity                              // 1.18.2
import net.minecraft.world.item.ItemStack                                   // 1.18.2
import net.minecraft.world.inventory.InventoryMenu                          // 1.18.2: PlayerContainer → InventoryMenu
import net.minecraft.core.Direction                                         // 1.18.2
import net.minecraft.resources.ResourceLocation                             // 1.18.2: net.minecraft.util.ResourceLocation → resources
import net.minecraft.world.phys.Vec3                                        // 1.18.2: Vector3d → Vec3
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.eventbus.api.SubscribeEvent

import scala.collection.JavaConverters.bufferAsJavaList
import scala.collection.mutable

object NetSplitterModel extends SmartBlockModelBase {
  override def getOverrides: ItemOverrides = ItemOverride

  override def getQuads(state: BlockState, side: Direction, rand: util.Random, data: IModelData): util.List[BakedQuad] =
    data match {
      case t: blockentity.NetSplitter =>
        val faces = mutable.ArrayBuffer.empty[BakedQuad]
        faces ++= BaseModel
        addSideQuads(faces, Direction.values().map(t.isSideOpen))
        bufferAsJavaList(faces)
      case _ => super.getQuads(state, side, rand)
    }

  // 1.18.2: AtlasTexture → TextureAtlas
  private def getSprite(location: ResourceLocation, atlas: Option[TextureAtlas]): TextureAtlasSprite = atlas match {
    case Some(atls) => atls.getSprite(location)
    case None       => Textures.getSprite(location)
  }

  protected def splitterTexture(atlas: Option[TextureAtlas]) = Array(
    getSprite(Textures.Block.NetSplitterTop,  atlas),
    getSprite(Textures.Block.NetSplitterTop,  atlas),
    getSprite(Textures.Block.NetSplitterSide, atlas),
    getSprite(Textures.Block.NetSplitterSide, atlas),
    getSprite(Textures.Block.NetSplitterSide, atlas),
    getSprite(Textures.Block.NetSplitterSide, atlas)
  )

  // 1.18.2: AtlasTexture → TextureAtlas
  protected def GenerateBaseModel(atlas: TextureAtlas) = {
    val faces = mutable.ArrayBuffer.empty[BakedQuad]
    // 1.18.2: new Vector3d(...) → new Vec3(...)
    // Bottom.
    faces ++= bakeQuads(makeBox(new Vec3(0/16f,  0/16f,  5/16f),  new Vec3(5/16f,  5/16f,  11/16f)), splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(11/16f, 0/16f,  5/16f),  new Vec3(16/16f, 5/16f,  11/16f)), splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(5/16f,  0/16f,  0/16f),  new Vec3(11/16f, 5/16f,  5/16f)),  splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(5/16f,  0/16f,  11/16f), new Vec3(11/16f, 5/16f,  16/16f)), splitterTexture(Some(atlas)), None)
    // Corners.
    faces ++= bakeQuads(makeBox(new Vec3(0/16f,  0/16f,  0/16f),  new Vec3(5/16f,  16/16f, 5/16f)),  splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(11/16f, 0/16f,  0/16f),  new Vec3(16/16f, 16/16f, 5/16f)),  splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(0/16f,  0/16f,  11/16f), new Vec3(5/16f,  16/16f, 16/16f)), splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(11/16f, 0/16f,  11/16f), new Vec3(16/16f, 16/16f, 16/16f)), splitterTexture(Some(atlas)), None)
    // Top.
    faces ++= bakeQuads(makeBox(new Vec3(0/16f,  11/16f, 5/16f),  new Vec3(5/16f,  16/16f, 11/16f)), splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(11/16f, 11/16f, 5/16f),  new Vec3(16/16f, 16/16f, 11/16f)), splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(5/16f,  11/16f, 0/16f),  new Vec3(11/16f, 16/16f, 5/16f)),  splitterTexture(Some(atlas)), None)
    faces ++= bakeQuads(makeBox(new Vec3(5/16f,  11/16f, 11/16f), new Vec3(11/16f, 16/16f, 16/16f)), splitterTexture(Some(atlas)), None)
    faces.toArray
  }

  protected var BaseModel = Array.empty[BakedQuad]

  @SubscribeEvent
  def onTextureStitch(e: TextureStitchEvent.Post): Unit = {
    // 1.18.2: getMap() は存在せず getAtlas() が正しいメソッド名（Forgeソース確認済み）
    // PlayerContainer.BLOCK_ATLAS → InventoryMenu.BLOCK_ATLAS
    if (e.getAtlas.location().equals(InventoryMenu.BLOCK_ATLAS)) BaseModel = GenerateBaseModel(e.getAtlas)
  }

  protected def addSideQuads(faces: mutable.ArrayBuffer[BakedQuad], openSides: Array[Boolean]): Unit = {
    val down  = openSides(Direction.DOWN.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(5/16f, if (down) 0/16f else 2/16f, 5/16f),   new Vec3(11/16f, 5/16f, 11/16f)),  splitterTexture(None), None)
    val up    = openSides(Direction.UP.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(5/16f, 11/16f, 5/16f), new Vec3(11/16f, if (up) 16/16f else 14f/16f, 11/16f)),  splitterTexture(None), None)
    val north = openSides(Direction.NORTH.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(5/16f, 5/16f, if (north) 0/16f else 2/16f),  new Vec3(11/16f, 11/16f, 5/16f)),  splitterTexture(None), None)
    val south = openSides(Direction.SOUTH.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(5/16f, 5/16f, 11/16f), new Vec3(11/16f, 11/16f, if (south) 16/16f else 14/16f)), splitterTexture(None), None)
    val west  = openSides(Direction.WEST.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(if (west) 0/16f else 2/16f, 5/16f, 5/16f),   new Vec3(5/16f, 11/16f, 11/16f)),  splitterTexture(None), None)
    val east  = openSides(Direction.EAST.ordinal())
    faces ++= bakeQuads(makeBox(new Vec3(11/16f, 5/16f, 5/16f), new Vec3(if (east) 16/16f else 14/16f, 11/16f, 11/16f)),  splitterTexture(None), None)
  }

  object ItemModel extends SmartBlockModelBase {
    override def getQuads(state: BlockState, side: Direction, rand: util.Random): util.List[BakedQuad] = {
      val faces = mutable.ArrayBuffer.empty[BakedQuad]
      faces ++= BaseModel
      addSideQuads(faces, Direction.values().map(_ => false))
      bufferAsJavaList(faces)
    }
  }

  object ItemOverride extends ItemOverrides {
    // 1.18.2: resolve に seed: Int 引数が追加された
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, seed: Int): BakedModel =
      ItemModel
  }
}