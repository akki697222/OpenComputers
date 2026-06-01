package li.cil.oc.integration.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import li.cil.oc.common.blockentity.Relay
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager, CapabilityToken}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.eventbus.api.SubscribeEvent
import li.cil.oc.OpenComputers
import net.minecraftforge.common.MinecraftForge

object PeripheralProvider {
  val CAPABILITY_PERIPHERAL: Capability[IPeripheral] =
    CapabilityManager.get(new CapabilityToken[IPeripheral]() {})

  private val PERIPHERAL_KEY = ResourceLocation.fromNamespaceAndPath(OpenComputers.ID, "peripheral")

  def register(): Unit = {
    if (!isComputerCraftPresent()) return
    MinecraftForge.EVENT_BUS.register(this)
  }

  private def isComputerCraftPresent(): Boolean = {
    try {
      Class.forName("dan200.computercraft.api.peripheral.IDynamicPeripheral",
        false, getClass.getClassLoader)
      true
    } catch {
      case _: ClassNotFoundException => false
    }
  }

  @SubscribeEvent
  def attachCapabilities(event: AttachCapabilitiesEvent[BlockEntity]): Unit = {
    event.getObject match {
      case relay: Relay =>
        val peripheral = new RelayPeripheral(relay)
        val lazyOptional = LazyOptional.of(() => peripheral)

        event.addCapability(PERIPHERAL_KEY, new net.minecraftforge.common.capabilities.ICapabilityProvider {
          override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] =
            if (cap == CAPABILITY_PERIPHERAL) lazyOptional.cast()
            else LazyOptional.empty()
        })

        event.addListener(() => lazyOptional.invalidate())
      case _ =>
    }
  }
}