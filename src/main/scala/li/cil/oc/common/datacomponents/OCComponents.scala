package li.cil.oc.common.datacomponents

import com.mojang.serialization.Codec
import li.cil.oc.api.ImmutableItemStack
import li.cil.oc.api.network.Visibility
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.server.component.DebugCard.AccessContext
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.core.{BlockPos, UUIDUtil}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.{Component, ComponentSerialization}
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ColorRGBA
import net.minecraft.world.item.DyeColor
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.registries.{DeferredHolder, DeferredRegister}

import java.nio.ByteBuffer
import java.util.UUID

object OCComponents {
  type Type[T] = DeferredHolder[DataComponentType[_], DataComponentType[T]]

  val REGISTRAR: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "opencomputers")

  private def persistent[T](name: String, codec: Codec[T]): Type[T] = {
    REGISTRAR.registerComponentType(name, _.persistent(codec))
  }

  private def persistentShared[T](name: String, codec: Codec[T], streamCodec: StreamCodec[_ >: RegistryFriendlyByteBuf, T]): Type[T] = {
    REGISTRAR.registerComponentType(name, _.persistent(codec).networkSynchronized(streamCodec))
  }

  val ID: Type[UUID] = persistentShared("id", UUIDUtil.CODEC, UUIDUtil.STREAM_CODEC)
  val OWNER: Type[Owner] = persistentShared("owner", Owner.CODEC, Owner.STREAM_CODEC)
  val DISK_COLOR: Type[DyeColor] = persistentShared("disk_color", DyeColor.CODEC, DyeColor.STREAM_CODEC)
  val LOOT_DISK: Type[ResourceLocation] = persistentShared("loot_disk", ResourceLocation.CODEC, ResourceLocation.STREAM_CODEC)
  val LABEL: Type[String] = persistentShared("label", Codec.STRING, ByteBufCodecs.STRING_UTF8)
  val READONLY: Type[Boolean] = persistentShared("readonly", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val EEPROM_CODE: Type[ByteBuffer] = persistent("eeprom_code", Codec.BYTE_BUFFER)
  val EEPROM_DATA: Type[ByteBuffer] = persistent("eeprom_data", Codec.BYTE_BUFFER)
  val MF_COORD: Type[MFCoords] = persistentShared("mf_coord", MFCoords.CODEC, MFCoords.STREAM_CODEC)
  val COMPONENTS: Type[List[ImmutableItemStack]] = persistentShared("components", ScalaCodec.list(ImmutableItemStack.OPTIONAL_CODEC), ScalaStreamCodec.list(ImmutableItemStack.OPTIONAL_STREAM_CODEC))
  val CONTENTS: Type[List[ImmutableItemStack]] = persistentShared("contents", ScalaCodec.list(ImmutableItemStack.OPTIONAL_CODEC), ScalaStreamCodec.list(ImmutableItemStack.OPTIONAL_STREAM_CODEC))
  val CONTAINERS: Type[List[ImmutableItemStack]] = persistentShared("containers", ScalaCodec.list(ImmutableItemStack.OPTIONAL_CODEC), ScalaStreamCodec.list(ImmutableItemStack.OPTIONAL_STREAM_CODEC))
  val ATTACHMENT: Type[ImmutableItemStack] = persistentShared("attachment", ImmutableItemStack.OPTIONAL_CODEC, ImmutableItemStack.OPTIONAL_STREAM_CODEC)
  val TIER: Type[Byte] = persistentShared("tier", ScalaCodec.BYTE, ScalaStreamCodec.BYTE)
  val STORED_ENERGY: Type[Int] = persistentShared("stored_energy", ScalaCodec.INT, ScalaStreamCodec.VAR_INT)
  val ADDRESS: Type[String] = persistentShared("address", Codec.STRING, ByteBufCodecs.STRING_UTF8)
  val VISIBILITY: Type[Visibility] = persistentShared("visibility", Visibility.CODEC, Visibility.STREAM_CODEC)
  val UNMANAGED: Type[Boolean] = persistentShared("unmanaged", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val LOCK: Type[String] = persistentShared("lock", Codec.STRING, ByteBufCodecs.STRING_UTF8)
  val CHARGE: Type[Double] = persistentShared("charge", ScalaCodec.DOUBLE, ScalaStreamCodec.DOUBLE)
  val MAX_CHARGE: Type[Double] = persistentShared("max_charge", ScalaCodec.DOUBLE, ScalaStreamCodec.DOUBLE)
  val ROBOT_CHARGE: Type[RobotChargeInfo] = persistentShared("robot_charge", RobotChargeInfo.CODEC, RobotChargeInfo.STREAM_CODEC)
  val NANOMACHINES_NETWORK_INFO: Type[CompoundTag] = persistent("nanomachines_network_info", CompoundTag.CODEC)
  val SOURCE_MAP_ITEM: Type[ImmutableItemStack] = persistent("source_map_item", ImmutableItemStack.OPTIONAL_CODEC)
  val KEYS: Type[List[String]] = persistentShared("keys", ScalaCodec.list(Codec.STRING), ScalaStreamCodec.list(ByteBufCodecs.STRING_UTF8))
  val TERMINAL_REFERENCE: Type[TerminalReference] = persistent("terminal_reference", TerminalReference.CODEC)
  val TEXT_BUFFER: Type[TextBufferContents] = persistent("text_buffer", TextBufferContents.CODEC)
  val IS_ON: Type[Boolean] = persistentShared("is_on", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val IS_RUNNING: Type[Boolean] = persistentShared("is_running", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val IS_POWERED: Type[Boolean] = persistentShared("is_powered", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val MAX_VIDEO_MODE: Type[MaximumVideoMode] = persistentShared("max_video_mode", MaximumVideoMode.CODEC, MaximumVideoMode.STREAM_CODEC)
  val VIDEO_MODE: Type[VideoMode] = persistentShared("video_mode", VideoMode.CODEC, VideoMode.STREAM_CODEC)
  val IS_PRECISE: Type[Boolean] = persistentShared("is_precise", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val MACHINE: Type[MachineData] = persistent("machine", MachineData.CODEC)
  val DRONE_STATE: Type[DroneState] = persistentShared("drone_state", DroneState.CODEC, DroneState.STREAM_CODEC)
  val STATUS_TEXT: Type[Component] = persistentShared("status_text", ComponentSerialization.FLAT_CODEC, ComponentSerialization.STREAM_CODEC)
  val LIGHT_COLOR: Type[ColorRGBA] = persistentShared("light_color", ColorRGBA.CODEC, ScalaStreamCodec.COLOR_RGBA)
  val PRINT: Type[PrintData] = persistentShared("print", PrintData.CODEC, PrintData.STREAM_CODEC)
  val FILESYSTEM_DATA: Type[CompoundTag] = persistent("filesystem", CompoundTag.CODEC)
  val ROBOT_ROM_FILESYSTEM_DATA: Type[CompoundTag] = persistent("robot_rom_filesystem", CompoundTag.CODEC)
  val HANDLES: Type[Map[String, Set[Int]]] = persistent("handles", ScalaCodec.map(Codec.STRING, ScalaCodec.set(ScalaCodec.INT)))
  val COMPOUND_DRIVER: Type[(Long, Map[String, CompoundStorage])] = persistent("compound_driver", ScalaCodec.pair(ScalaCodec.LONG -> ScalaCodec.map(Codec.STRING, CompoundStorage.CODEC)))
  val PALETTE: Type[Array[Int]] = persistentShared("palette", ScalaCodec.INT_ARRAY, ScalaStreamCodec.INT_ARRAY)
  val GRAPHICS_CARD: Type[GraphicsCardState] = persistent("graphics_card", GraphicsCardState.CODEC)
  val VIDEO_RAM: Type[List[(Int, CompoundStorage)]] = persistent("video_ram", ScalaCodec.list(ScalaCodec.pair(ScalaCodec.INT -> CompoundStorage.CODEC)))
  val WAKE_THRESHOLD: Type[Int] = persistent("wake_threshold", ScalaCodec.INT)
  val WIRELESS_REDSTONE_STATE: Type[WirelessRedstoneState] = persistent("wireless_redstone_state", WirelessRedstoneState.CODEC)
  val LEASHED_ENTITIES: Type[List[UUID]] = persistent("leashed_entities", ScalaCodec.list(UUIDUtil.CODEC))
  val OPEN_PORTS: Type[Array[Int]] = persistent("open_ports", ScalaCodec.INT_ARRAY)
  val WAKE_MESSAGE: Type[WakeMessage] = persistent("wake_message", WakeMessage.CODEC)
  val TUNNEL: Type[String] = persistent("tunnel", Codec.STRING)
  val STRENGTH: Type[Double] = persistent("strength", ScalaCodec.DOUBLE)
  val HEAD_POS: Type[Int] = persistent("head_position", ScalaCodec.INT)
  val TANK: Type[FluidStack] = persistent("tank", FluidStack.CODEC)
  val FUEL_INVENTORY: Type[ImmutableItemStack] = persistent("fuel_inventory", ImmutableItemStack.OPTIONAL_CODEC)
  val FUEL_TICKS_REMAINING: Type[Int] = persistent("fuel_ticks_remaining", ScalaCodec.INT)
  val DEBUG_CARD_ACCESS_CONTEXT: Type[AccessContext] = persistent("debug_card/access_context", AccessContext.CODEC)
  val DEBUG_CARD_REMOTE_NODE_POSITION: Type[BlockPos] = persistent("debug_card/remote_node", BlockPos.CODEC)
}
