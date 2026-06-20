import li.cil.oc.common.{Tier, TierMigration}
import net.minecraft.nbt.CompoundTag
import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TierMigrationTest extends AnyFunSpec with Matchers {
  private val TierTag = "opencomputers:tier"

  describe("TierMigration") {
    it("migrates legacy creative tier 3 to CE tier 4") {
      val nbt = new CompoundTag()
      nbt.putByte(TierTag, Tier.Four.toByte)

      TierMigration.loadStoredTier(nbt, TierTag) shouldBe Tier.Five
    }

    it("keeps CE tier 3 when dataVersion is current") {
      val nbt = new CompoundTag()
      nbt.putInt(TierMigration.DataVersionTag, TierMigration.CurrentDataVersion)
      nbt.putByte(TierTag, Tier.Four.toByte)

      TierMigration.loadStoredTier(nbt, TierTag) shouldBe Tier.Four
    }

    it("keeps CE creative tier 4 on reload") {
      val nbt = new CompoundTag()
      nbt.putInt(TierMigration.DataVersionTag, TierMigration.CurrentDataVersion)
      nbt.putByte(TierTag, Tier.Five.toByte)

      TierMigration.loadStoredTier(nbt, TierTag) shouldBe Tier.Five
    }

    it("writes dataVersion on save") {
      val nbt = new CompoundTag()
      TierMigration.saveStoredTier(nbt, TierTag, Tier.Four, asByte = true)

      nbt.getInt(TierMigration.DataVersionTag) shouldBe TierMigration.CurrentDataVersion
      nbt.getByte(TierTag) shouldBe Tier.Four.toByte
    }
  }
}
