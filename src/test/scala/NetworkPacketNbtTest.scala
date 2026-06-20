import li.cil.oc.Settings
import li.cil.oc.server.network.Network
import net.minecraft.nbt.{ByteTag, CompoundTag, IntTag, StringTag}
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.lang.Boolean.{FALSE => JFalse, TRUE => JTrue}
import java.nio.file.Files

@RunWith(classOf[JUnitRunner])
class NetworkPacketNbtTest extends AnyFunSpec with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    Settings.load(Files.createTempFile("oc-network-packet-test", ".conf").toFile)
  }

  describe("Network.newPacket(nbt)") {
    it("reads destination when dest key is present") {
      val nbt = new CompoundTag()
      nbt.putString("source", "src")
      nbt.putString("dest", "target")
      nbt.putInt("port", 1)
      nbt.putInt("ttl", 10)
      nbt.putInt("dataLength", 0)

      val packet = Network.newPacket(nbt)
      packet.destination shouldBe "target"
    }

    it("deserializes boolean packet parts with OC 1.12 semantics (byte == 1)") {
      val nbt = new CompoundTag()
      nbt.putString("source", "src")
      nbt.putInt("port", 1)
      nbt.putInt("ttl", 10)
      nbt.putInt("dataLength", 2)
      nbt.putByte("data0", 1.toByte)
      nbt.putByte("data1", 0.toByte)

      val packet = Network.newPacket(nbt)
      packet.data(0) shouldBe JTrue
      packet.data(1) shouldBe JFalse
    }
  }
}
