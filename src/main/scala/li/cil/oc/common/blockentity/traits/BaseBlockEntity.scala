package li.cil.oc.common.blockentity.traits

import li.cil.oc.{OpenComputers, Settings}
import li.cil.oc.client.Sound
import li.cil.oc.common.SaveHandler
import li.cil.oc.util.{BlockPosition, SideTracker}
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.neoforged.api.distmarker.{Dist, OnlyIn}
import net.neoforged.neoforge.client.model.data.ModelProperty

trait BaseBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
  private final val IsServerDataTag = Settings.namespace + "isServerData"

  def x: Int = getBlockPos.getX

  def y: Int = getBlockPos.getY

  def z: Int = getBlockPos.getZ

  def position = BlockPosition(x, y, z, getLevel)

  def isClient: Boolean = !isServer

  def isServer: Boolean = if (getLevel != null) !getLevel.isClientSide else SideTracker.isServer

  // ----------------------------------------------------------------------- //

  def updateEntity(): Unit = {
    if (Settings.get.periodicallyForceLightUpdate && getLevel.getGameTime % 40 == 0 && getBlockState.getLightEmission(getLevel, getBlockPos) > 0) {
      getLevel.sendBlockUpdated(getBlockPos, getLevel.getBlockState(getBlockPos), getLevel.getBlockState(getBlockPos), 3)
    }
  }

  override def clearRemoved(): Unit = {
    super.clearRemoved()
    initialize()
  }

  override def setRemoved(): Unit = {
    super.setRemoved()
    dispose()
  }

  override def onChunkUnloaded(): Unit = {
    super.onChunkUnloaded()
    try dispose() catch {
      case t: Throwable => OpenComputers.log.error("Failed properly disposing a tile entity, things may leak and or break.", t)
    }
  }

  protected def initialize(): Unit = {
  }

  def dispose(): Unit = {
    if (isClient) {
      // Note: chunk unload is handled by sound via event handler.
      Sound.stopLoop(this)
    }
  }

  // ----------------------------------------------------------------------- //

  def loadForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {}

  def saveForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.putBoolean(IsServerDataTag, true)
    super.saveAdditional(nbt, provider)
  }

  @OnlyIn(Dist.CLIENT)
  def loadForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {}

  @OnlyIn(Dist.CLIENT)
  def saveForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.putBoolean(IsServerDataTag, false)
  }

  // ----------------------------------------------------------------------- //

  override def loadAdditional(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadAdditional(nbt, provider)
    if (isServer || nbt.getBoolean(IsServerDataTag)) {
      loadForServer(nbt, provider)
    } else {
      loadForClient(nbt, provider)
    }
  }

  override def saveAdditional(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveAdditional(nbt, provider)
    save(nbt, provider)
  }

  def save(nbt: CompoundTag, provider: HolderLookup.Provider): CompoundTag = {
    if (isServer) {
      saveForServer(nbt, provider)
    }
    nbt
  }

  override def getUpdatePacket: ClientboundBlockEntityDataPacket = {
    ClientboundBlockEntityDataPacket.create(this)
  }

  override def getUpdateTag(provider: HolderLookup.Provider): CompoundTag = {
    val nbt = super.getUpdateTag(provider)

    // See comment on savingForClients variable.
    SaveHandler.savingForClients = true
    try {
      try saveForClient(nbt, provider) catch {
        case e: Throwable => OpenComputers.log.warn("There was a problem writing a TileEntity description packet. Please report this if you see it!", e)
      }
    } finally {
      SaveHandler.savingForClients = false
    }

    nbt
  }

  override def onDataPacket(manager: Connection, packet: ClientboundBlockEntityDataPacket, provider: HolderLookup.Provider): Unit = {
    try loadForClient(packet.getTag, provider) catch {
      case e: Throwable => OpenComputers.log.warn("There was a problem reading a TileEntity description packet. Please report this if you see it!", e)
    }
  }
  
  def hasProperty(prop: ModelProperty[_]) = false

  def getData[T](prop: ModelProperty[T]): T = null.asInstanceOf[T]

  def setData[T](prop: ModelProperty[T], value: T): T = null.asInstanceOf[T]
}
