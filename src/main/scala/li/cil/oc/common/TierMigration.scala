package li.cil.oc.common

import li.cil.oc.Settings
import net.minecraft.nbt.{ByteTag, CompoundTag, IntTag, Tag}

/** Maps tier values from OC 1.12 saves (creative = Tier.Four / 3) to CE (creative = Tier.Five / 4). */
object TierMigration {
  final val CurrentDataVersion = 1
  final val DataVersionTag = Settings.namespace + "dataVersion"

  /** Legacy OC 1.12 stored creative tier as 3; only that value needs migration when dataVersion is absent. */
  def loadStoredTier(nbt: CompoundTag, tierTag: String, maxTier: Int = Tier.Five): Int = {
    val stored = readTierValue(nbt, tierTag)
    val version = if (nbt.contains(DataVersionTag)) nbt.getInt(DataVersionTag) else 0
    val migrated = if (version < CurrentDataVersion && stored == Tier.Four) Tier.Five else stored
    migrated max 0 min maxTier
  }

  def saveStoredTier(nbt: CompoundTag, tierTag: String, tier: Int, asByte: Boolean): Unit = {
    nbt.putInt(DataVersionTag, CurrentDataVersion)
    if (asByte) nbt.putByte(tierTag, tier.toByte)
    else nbt.putInt(tierTag, tier)
  }

  private def readTierValue(nbt: CompoundTag, tierTag: String): Int = {
    if (!nbt.contains(tierTag)) return 0
    nbt.get(tierTag).getId match {
      case Tag.TAG_BYTE => nbt.getByte(tierTag)
      case Tag.TAG_INT => nbt.getInt(tierTag)
      case _ => nbt.getInt(tierTag)
    }
  }
}
