package li.cil.oc.common.datacomponents

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor
import net.neoforged.neoforge.registries.{DeferredHolder, DeferredRegister}

import java.nio.ByteBuffer
import java.util.function.Supplier

object OCComponents {
  type Type[T] = DeferredHolder[DataComponentType[_], DataComponentType[T]]
  
  val REGISTRAR: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "opencomputers")
  
  private def persistent[T](name: String, codec: Codec[T]): Type[T] = {
    return REGISTRAR.registerComponentType(name, _.persistent(codec))
  }

  private def persistentShared[T](name: String, codec: Codec[T], streamCodec: StreamCodec[ByteBuf, T]): Type[T] = {
    return REGISTRAR.registerComponentType(name, _.persistent(codec).networkSynchronized(streamCodec))
  }

  val DISK_COLOR: Type[DyeColor] = persistentShared("disk_color", DyeColor.CODEC, DyeColor.STREAM_CODEC)
  val LOOT_DISK: Type[ResourceLocation] = persistentShared("loot_disk", ResourceLocation.CODEC, ResourceLocation.STREAM_CODEC)
  val LABEL: Type[String] = persistentShared("label", Codec.STRING, ByteBufCodecs.STRING_UTF8)
  val READONLY: Type[Boolean] = persistentShared("readonly", ScalaCodec.BOOL, ScalaStreamCodec.BOOL)
  val EEPROM_CODE: Type[ByteBuffer] = persistent("eeprom_code", Codec.BYTE_BUFFER)
  val EEPROM_DATA: Type[ByteBuffer] = persistent("eeprom_data", Codec.BYTE_BUFFER)
}
