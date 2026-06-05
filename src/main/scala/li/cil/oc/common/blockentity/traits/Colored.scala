package li.cil.oc.common.blockentity.traits

import li.cil.oc.Settings
import li.cil.oc.api.internal
import li.cil.oc.server.PacketSender
import li.cil.oc.util.Color
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.DyeColor
import net.minecraft.nbt.CompoundTag
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

trait Colored extends BaseBlockEntity with internal.Colored {
  private var _color = 0

  def consumesDye = false

  override def getColor: Int = _color

  override def setColor(value: Int) = if (value != _color) {
    _color = value
    onColorChanged()
  }

  override def controlsConnectivity = false

  protected def onColorChanged(): Unit = {
    if (getLevel != null && isServer) {
      PacketSender.sendColorChange(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val RenderColorTag = Settings.namespace + "renderColorRGB"
  private final val RenderColorTagCompat = Settings.namespace + "renderColor"

  override def loadForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadForServer(nbt, provider)
    if (nbt.contains(RenderColorTagCompat)) {
      _color = Color.rgbValues(DyeColor.byId(nbt.getInt(RenderColorTagCompat)))
    }
    if (nbt.contains(RenderColorTag)) {
      _color = nbt.getInt(RenderColorTag)
    }
  }

  override def saveForServer(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveForServer(nbt, provider)
    nbt.putInt(RenderColorTag, _color)
  }

  @OnlyIn(Dist.CLIENT)
  override def loadForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.loadForClient(nbt, provider)
    if (nbt.contains(RenderColorTag)) {
      _color = nbt.getInt(RenderColorTag)
    }
  }

  @OnlyIn(Dist.CLIENT)
  override def saveForClient(nbt: CompoundTag, provider: HolderLookup.Provider): Unit = {
    super.saveForClient(nbt, provider)
    nbt.putInt(RenderColorTag, _color)
  }
}
