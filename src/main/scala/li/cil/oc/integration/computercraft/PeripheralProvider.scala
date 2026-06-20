package li.cil.oc.integration.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import li.cil.oc.OpenComputers
import li.cil.oc.common.blockentity.{Relay, BlockEntityTypes}
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.capabilities.{BlockCapability, ICapabilityProvider, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.common.NeoForge

object PeripheralProvider {
  // NeoForge 1.21.1: BlockCapability replaces the old CapabilityManager/CapabilityToken pattern
  val CAPABILITY_PERIPHERAL: BlockCapability[IPeripheral, Direction] =
    BlockCapability.createSided(
      ResourceLocation.fromNamespaceAndPath(OpenComputers.ID, "peripheral"),
      classOf[IPeripheral]
    )

  def register(): Unit = {
    if (!isComputerCraftPresent()) return
    // The RegisterCapabilitiesEvent listener must be on the MOD event bus, not FORGE bus.
    // This is called from the mod's mod-bus setup; if using a separate mod-bus object,
    // register via the mod event bus directly.
    OpenComputers.proxy.modBus.register(this)
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

  // NeoForge 1.21.1: RegisterCapabilitiesEvent replaces AttachCapabilitiesEvent.
  // This must be registered on the MOD event bus, not the FORGE event bus.
  @SubscribeEvent
  def onRegisterCapabilities(event: RegisterCapabilitiesEvent): Unit = {
    event.registerBlockEntity(
      CAPABILITY_PERIPHERAL,
      BlockEntityTypes.RELAY.get(),
      (relay: Relay, _: Direction) => new RelayPeripheral(relay): IPeripheral
    )
  }
}
