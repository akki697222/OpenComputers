package li.cil.oc.common.blockentity.traits

import li.cil.oc.api.Persistable
import li.cil.oc.{OpenComputers, Settings}
import li.cil.oc.client.Sound
import li.cil.oc.common.SaveHandler
import li.cil.oc.util.{BlockPosition, SideTracker}
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.{DataComponentHolder, DataComponentMap, DataComponentPatch, DataComponentType, PatchedDataComponentMap}
import net.minecraft.nbt.{CompoundTag, NbtOps}
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.neoforged.api.distmarker.{Dist, OnlyIn}
import net.neoforged.neoforge.client.model.data.ModelProperty
import net.neoforged.neoforge.common.MutableDataComponentHolder

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

  @deprecatedOverriding("use loadComponentsForServer()", since = "NeoForge 1.21+")
  def loadForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {}

  @deprecatedOverriding("use saveComponentsForServer()", since = "NeoForge 1.21+")
  def saveForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.putBoolean(IsServerDataTag, true)
    super.saveAdditional(nbt, provider)
  }

  @OnlyIn(Dist.CLIENT)
  @deprecatedOverriding("use loadComponentsForClient()", since = "NeoForge 1.21+")
  def loadForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {}

  @OnlyIn(Dist.CLIENT)
  @deprecatedOverriding("use saveComponentsForClient()", since = "NeoForge 1.21+")
  def saveForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    nbt.putBoolean(IsServerDataTag, false)
  }
  
  def loadComponentsCommon(holder: DataComponentHolder): Unit = {}
  def saveComponentsCommon(holder: MutableDataComponentHolder): Unit = {}
  def loadComponentsForServer(holder: DataComponentHolder): Unit = {}
  def saveComponentsForServer(holder: MutableDataComponentHolder): Unit = {}

  @OnlyIn(Dist.CLIENT)
  def loadComponentsForClient(holder: DataComponentHolder): Unit = {}
  @OnlyIn(Dist.CLIENT)
  def saveComponentsForClient(holder: MutableDataComponentHolder): Unit = {}

  // ----------------------------------------------------------------------- //

  override def loadAdditional(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadAdditional(nbt, provider)
    if (isServer || nbt.getBoolean(IsServerDataTag)) {
      loadForServer(nbt, provider)
    } else {
      loadForClient(nbt, provider)
    }
  }

  override def loadWithComponents(tag: CompoundTag, registries: HolderLookup.Provider): Unit = {
    super.loadWithComponents(tag, registries)
    // components are loaded here
    val holder = Persistable.holder(this)
    loadComponentsCommon(holder)

    if(isServer) {
      loadComponentsForServer(holder)
    } else {
      loadComponentsForClient(holder)
    }
  }

  override def saveAdditional(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveAdditional(nbt, provider)
    save(nbt, provider)

    val holder = Persistable.holder(this)
    saveComponentsCommon(holder)

    if(isServer) {
      saveComponentsForServer(holder)
    } else {
      saveComponentsForClient(holder)
    }
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

  private def dataComponentMap: PatchedDataComponentMap = components() match {
    case patched: PatchedDataComponentMap => patched
    case notPatched => {
      val patched = new PatchedDataComponentMap(notPatched)
      setComponents(patched)
      patched
    }
  }
}
