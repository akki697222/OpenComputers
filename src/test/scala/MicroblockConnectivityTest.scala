import li.cil.oc.server.network.MicroblockConnectivity
import net.minecraft.core.Direction
import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MicroblockConnectivityTest extends AnyFunSpec with Matchers {
  describe("MicroblockConnectivity") {
    it("allows connection when tile entity is null") {
      MicroblockConnectivity.canConnectFromSide(null, Direction.NORTH) shouldBe true
    }
  }
}
