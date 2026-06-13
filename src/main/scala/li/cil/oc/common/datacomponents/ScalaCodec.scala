package li.cil.oc.common.datacomponents

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}

object ScalaCodec {
  val BOOL: Codec[Boolean] = Codec.BOOL.xmap(b => b, b => b)
  val INT: Codec[Int] = Codec.INT.xmap(b => b, b => b)
}

object ScalaStreamCodec {
  val BOOL: StreamCodec[ByteBuf, Boolean] = ByteBufCodecs.BOOL.map(b => b, b => b)
  val INT: StreamCodec[ByteBuf, Int] = ByteBufCodecs.INT.map(b => b, b => b)
  val VAR_INT: StreamCodec[ByteBuf, Int] = ByteBufCodecs.VAR_INT.map(b => b, b => b)
}
