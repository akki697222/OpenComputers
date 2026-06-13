package li.cil.oc.common.block

import com.mojang.serialization.MapCodec
import li.cil.oc.api.Items
import li.cil.oc.common.block.ChameliumBlock.CODEC
import li.cil.oc.{CreativeTab, OpenComputers}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.BlockBehaviour.{Properties, simpleCodec}
import net.minecraft.world.item.context.{BlockPlaceContext => BlockItemUseContext}
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.{StateDefinition => StateContainer}
import net.minecraft.core.BlockPos
import net.minecraft.world.level.{LevelReader, BlockGetter => IBlockReader}
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber

class ChameliumBlock(props: Properties) extends SimpleBlock(props) {
  override def codec(): MapCodec[ChameliumBlock] = CODEC

  protected override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]): Unit = {
    builder.add(ChameliumBlock.Color)
  }
  registerDefaultState(stateDefinition.any.setValue(ChameliumBlock.Color, DyeColor.BLACK))

  override def getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState): ItemStack = {
    val stack = new ItemStack(this)
    stack.setDamageValue(state.getValue(ChameliumBlock.Color).getId)
    stack
  }

  override def getStateForPlacement(ctx: BlockItemUseContext): BlockState =
    defaultBlockState.setValue(ChameliumBlock.Color, DyeColor.byId(ctx.getItemInHand.getDamageValue))
}

object ChameliumBlock {
  final val CODEC = simpleCodec(new ChameliumBlock(_))
  final val Color = EnumProperty.create("color", classOf[DyeColor])

  @SubscribeEvent
  def onBuildCreativeTab(e: BuildCreativeModeTabContentsEvent): Unit = {
    if (e.getTabKey == CreativeTab.CREATIVE_TABS.getRegistryKey) {
      val stack = Items.get("opencomputers:chamelium_block").createItemStack(1)
      stack.setDamageValue(DyeColor.WHITE.getId)
      e.accept(stack)
    }
  }
}
