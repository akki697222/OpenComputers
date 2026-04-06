package li.cil.oc.common.block

import java.util
import li.cil.oc.common.block.property.PropertyCableConnection
import li.cil.oc.common.capabilities.Capabilities
import li.cil.oc.common.blockentity
import li.cil.oc.util.Color
import li.cil.oc.util.ExtendedLevel._
import li.cil.oc.util.ItemColorizer
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.{Player => PlayerEntity}
import net.minecraft.world.entity.{Entity, LivingEntity}
import net.minecraft.world.item.context.{BlockPlaceContext => BlockItemUseContext}
import net.minecraft.world.item.{DyeColor, ItemStack}
import net.minecraft.world.level.block.state.{StateDefinition => StateContainer}
import net.minecraft.world.level.block.entity.{BlockEntity => TileEntity}
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.{HitResult => RayTraceResult}
import net.minecraft.world.phys.shapes.{CollisionContext => ISelectionContext}
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.{Shapes => VoxelShapes}
import net.minecraft.world.level.{BlockGetter => IBlockReader}
import net.minecraft.world.level.{LevelAccessor => IWorld}
import net.minecraft.world.level.{Level => World}
import net.minecraft.server.level.{ServerLevel => ServerWorld}
import net.minecraft.world.level.block.state.properties.Property
import net.minecraftforge.common.extensions.IForgeBlock

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

class Cable(props: Properties) extends SimpleBlock(props) with IForgeBlock {
  // For Immibis Microblock support.
  val ImmibisMicroblocks_TransformableBlockMarker = null

  // For FMP part coloring.
  var colorMultiplierOverride: Option[Int] = None

  // ----------------------------------------------------------------------- //
  
  registerDefaultState(CableHelper.helperRegisterDefaultState(this.stateDefinition))

  override def getStateForPlacement(ctx: BlockItemUseContext): BlockState = {
    val color = Cable.getConnectionColor(ctx.getItemInHand)
    val fromPos = new BlockPos.MutableBlockPos()
    Direction.values.foldLeft(defaultBlockState)((state, fromSide) => {
      fromPos.setWithOffset(ctx.getClickedPos, fromSide)
      val fromState = ctx.getLevel.getBlockState(fromPos)
      Cable.updateState(state, null, color, fromSide, fromState, ctx.getLevel, fromPos)
    })
  }

  override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]): Unit = {
    builder.add(
      PropertyCableConnection.DOWN,
      PropertyCableConnection.UP,
      PropertyCableConnection.NORTH,
      PropertyCableConnection.SOUTH,
      PropertyCableConnection.WEST,
      PropertyCableConnection.EAST
    )
  }

  override def getCloneItemStack(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity) =
    world.getBlockEntity(pos) match {
      case t: blockentity.Cable => t.createItemStack()
      case _ => createItemStack()
    }

  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, ctx: ISelectionContext): VoxelShape = Cable.shape(state)

  override def neighborChanged(state: BlockState, world: World, pos: BlockPos, other: Block, otherPos: BlockPos, moved: Boolean): Unit = {
    if (world.isClientSide) return
    val newState = world.getBlockEntity(pos) match {
      case t: blockentity.Cable => {
        val fromPos = new BlockPos.MutableBlockPos()
        Direction.values.foldLeft(state)((state, fromSide) => {
          fromPos.setWithOffset(pos, fromSide)
          val fromState = world.getBlockState(fromPos)
          Cable.updateState(state, t, -1, fromSide, fromState, world, fromPos)
        })
      }
      case _ => state
    }
    if (newState != state) world.setBlock(pos, newState, 0x13)
  }

  override def updateShape(state: BlockState, fromSide: Direction, fromState: BlockState, world: IWorld, pos: BlockPos, fromPos: BlockPos): BlockState =
    Cable.updateState(state, world.getBlockEntity(pos), -1, fromSide, fromState, world, fromPos)

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Cable(pos, state)

  // ----------------------------------------------------------------------- //

  override def setPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.setPlacedBy(world, pos, state, placer, stack)
    world.getBlockEntity(pos) match {
      case tileEntity: blockentity.Cable => {
        tileEntity.fromItemStack(stack)
        state.updateNeighbourShapes(world, pos, 2)
      }
      case _ =>
    }
  }
}

object Cable {
  final val MIN = 0.375
  final val MAX = 1 - MIN

  final val DefaultShape: VoxelShape = VoxelShapes.box(MIN, MIN, MIN, MAX, MAX, MAX)

  final val CachedParts: Array[VoxelShape] = Array(
    VoxelShapes.box( MIN, 0, MIN, MAX, MIN, MAX ), // Down
    VoxelShapes.box( MIN, MAX, MIN, MAX, 1, MAX ), // Up
    VoxelShapes.box( MIN, MIN, 0, MAX, MAX, MIN ), // North
    VoxelShapes.box( MIN, MIN, MAX, MAX, MAX, 1 ), // South
    VoxelShapes.box( 0, MIN, MIN, MIN, MAX, MAX ), // West
    VoxelShapes.box( MAX, MIN, MIN, 1, MAX, MAX )) // East

  final val CachedBounds = {
    // 6 directions = 6 bits = 11111111b >> 2 = 0xFF >> 2
    (0 to 0xFF >> 2).map(mask => {
      Direction.values.foldLeft(DefaultShape)((shape, side) => {
        if (((1 << side.get3DDataValue) & mask) != 0) VoxelShapes.or(shape, CachedParts(side.ordinal()))
        else shape
      })
    }).toArray
  }

  def mask(side: Direction, value: Int = 0) = value | (1 << side.get3DDataValue)

  def shape(state: BlockState): VoxelShape = {
    var result = 0
    for (side <- Direction.values) {
      val sideShape = CableHelper.getCableShape(state, side)
      if (sideShape != PropertyCableConnection.Shape.NONE) {
        result = mask(side, result)
      }
    }
    Cable.CachedBounds(result)
  }

  def updateState(state: BlockState, tileEntity: TileEntity, defaultColor: Int, fromSide: Direction, fromState: BlockState, world: IBlockReader, fromPos: BlockPos): BlockState = {
    val prop = PropertyCableConnection.BY_DIRECTION.get(fromSide)
    val neighborTileEntity = world.getBlockEntity(fromPos)
    if (neighborTileEntity != null && neighborTileEntity.getLevel != null) {
      val neighborHasNode = hasNetworkNode(neighborTileEntity, fromSide.getOpposite)
      val canConnectColor = canConnectBasedOnColor(tileEntity, neighborTileEntity, defaultColor)
      val canConnectIM = canConnectFromSideIM(tileEntity, fromSide) && canConnectFromSideIM(neighborTileEntity, fromSide.getOpposite)
      if (neighborHasNode && canConnectColor && canConnectIM) {
        if (fromState.is(state.getBlock)) {
          return CableHelper.helperSetCableShapeState(state, fromSide, PropertyCableConnection.Shape.CABLE)
        }
        else {
          return CableHelper.helperSetCableShapeState(state, fromSide, PropertyCableConnection.Shape.DEVICE)
        }
      }
    }
    CableHelper.helperSetCableShapeState(state, fromSide, PropertyCableConnection.Shape.NONE)
  }

  private def hasNetworkNode(tileEntity: TileEntity, side: Direction): Boolean = {
    if (tileEntity != null) {
      if (tileEntity.isInstanceOf[blockentity.RobotProxy]) return false

      if (tileEntity.getCapability(Capabilities.SidedEnvironmentCapability, side).isPresent) {
        val host = tileEntity.getCapability(Capabilities.SidedEnvironmentCapability, side).orElse(null)
        if (host != null) {
          return if (tileEntity.getLevel.isClientSide) host.canConnect(side) else host.sidedNode(side) != null
        }
      }

      if (tileEntity.getCapability(Capabilities.EnvironmentCapability, side).isPresent) {
        val host = tileEntity.getCapability(Capabilities.EnvironmentCapability, side)
        if (host.isPresent) return true
      }
    }

    false
  }

  private def getConnectionColor(stack: ItemStack): Int = {
    val color = ItemColorizer.getColor(stack)
    if (color == -1) Color.rgbValues(DyeColor.LIGHT_GRAY) else color
  }

  private def getConnectionColor(tileEntity: TileEntity): Int = {
    if (tileEntity != null) {
      if (tileEntity.getCapability(Capabilities.ColoredCapability, null).isPresent) {
        val colored = tileEntity.getCapability(Capabilities.ColoredCapability, null).orElse(null)
        if (colored != null && colored.controlsConnectivity) return colored.getColor
      }
    }

    Color.rgbValues(DyeColor.LIGHT_GRAY)
  }

  private def canConnectBasedOnColor(te1: TileEntity, te2: TileEntity, c1Default: Int = Color.rgbValues(DyeColor.LIGHT_GRAY)) = {
    val (c1, c2) = (if (te1 == null) c1Default else getConnectionColor(te1), getConnectionColor(te2))
    c1 == c2 || c1 == Color.rgbValues(DyeColor.LIGHT_GRAY) || c2 == Color.rgbValues(DyeColor.LIGHT_GRAY)
  }

  private def canConnectFromSideIM(tileEntity: TileEntity, side: Direction) =
    tileEntity match {
      case im: blockentity.traits.ImmibisMicroblock => im.ImmibisMicroblocks_isSideOpen(side.ordinal)
      case _ => true
    }
}
