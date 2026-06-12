package li.cil.oc.common.block

import java.util
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.Constants
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.blockentity
import li.cil.oc.integration.util.Wrench
import li.cil.oc.util.PackedColor
import li.cil.oc.util.Tooltip
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component => ITextComponent}
import net.minecraft.world.{InteractionHand => Hand}
import net.minecraft.world.entity.player.{Player => PlayerEntity}
import net.minecraft.world.item.{ItemStack, TooltipFlag => ITooltipFlag}
import net.minecraft.world.item.context.{BlockPlaceContext => BlockItemUseContext}
import net.minecraft.world.level.{BlockGetter => IBlockReader, Level => World}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.{BlockState, StateDefinition => StateContainer}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.phys.shapes.{CollisionContext => ISelectionContext, Shapes => VoxelShapes, VoxelShape}

import scala.collection.convert.ImplicitConversionsToScala._

class HoloScreen(props: Properties, tier: Int) extends Screen(props, tier) {
  private val Shape = VoxelShapes.box(0, 0, 0, 1, 0.5, 1)

  override protected def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Facing)

  override def getStateForPlacement(ctx: BlockItemUseContext): BlockState =
    super.getStateForPlacement(ctx).setValue(PropertyRotatable.Facing, ctx.getHorizontalDirection.getOpposite)

  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, ctx: ISelectionContext): VoxelShape = Shape

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.HoloScreen(pos, state, tier)

  override def getValidRotations(world: World, pos: BlockPos): Array[Direction] =
    Array(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)

  override def localOnBlockActivated(world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean =
    if (Wrench.holdsApplicableWrench(player, pos) || api.Items.get(heldItem) == api.Items.get(Constants.ItemName.Analyzer)) {
      super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
    }
    else if (player.isCrouching && heldItem.isEmpty) {
      world.getBlockEntity(pos) match {
        case screen: blockentity.HoloScreen =>
          val changed = screen.resize(side, world.getBlockState(pos).getValue(PropertyRotatable.Facing))
          if (changed) {
            world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3)
          }
          true
        case _ => false
      }
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)

  override protected def tooltipBody(stack: ItemStack, world: IBlockReader, tooltip: util.List[ITextComponent], advanced: ITooltipFlag): Unit = {
    val (w, h) = Settings.screenResolutionsByTier(tier)
    val depth = PackedColor.Depth.bits(Settings.screenDepthsByTier(tier))
    for (curr <- Tooltip.get("screen", w, h, depth)) {
      tooltip.add(ITextComponent.literal(curr).setStyle(Tooltip.DefaultStyle))
    }
    for (curr <- Tooltip.extended("holoscreen")) {
      tooltip.add(ITextComponent.literal(curr).setStyle(Tooltip.DefaultStyle))
    }
  }
}
