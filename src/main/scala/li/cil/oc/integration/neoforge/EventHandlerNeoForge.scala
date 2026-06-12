package li.cil.oc.integration.neoforge

import li.cil.oc.common.blockentity.traits.{BaseBlockEntity, PowerAcceptor}
import li.cil.oc.common.blockentity.TileEntityTypes
import li.cil.oc.integration.util.Power
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.neoforged.neoforge.capabilities.{Capabilities, ICapabilityProvider, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.energy.IEnergyStorage

object EventHandlerNeoForge {

  // Called from EventHandler.onRegisterCapabilities
  def onRegisterCapabilities(event: RegisterCapabilitiesEvent): Unit = {
    // Register IEnergyStorage (EnergyStorage.BLOCK) for all PowerAcceptor block entities
    Seq(
      TileEntityTypes.ADAPTER, TileEntityTypes.ASSEMBLER, TileEntityTypes.CABLE,
      TileEntityTypes.CAPACITOR, TileEntityTypes.CARPETED_CAPACITOR, TileEntityTypes.CASE,
      TileEntityTypes.CHARGER, TileEntityTypes.DISASSEMBLER, TileEntityTypes.DISK_DRIVE,
      TileEntityTypes.GEOLYZER, TileEntityTypes.HOLOGRAM, TileEntityTypes.KEYBOARD,
      TileEntityTypes.MICROCONTROLLER, TileEntityTypes.MOTION_SENSOR, TileEntityTypes.NET_SPLITTER,
      TileEntityTypes.POWER_CONVERTER, TileEntityTypes.POWER_DISTRIBUTOR, TileEntityTypes.PRINT,
      TileEntityTypes.PRINTER, TileEntityTypes.RACK, TileEntityTypes.RAID,
      TileEntityTypes.REDSTONE_IO, TileEntityTypes.RELAY, TileEntityTypes.ROBOT,
      TileEntityTypes.SCREEN, TileEntityTypes.TRANSPOSER, TileEntityTypes.WAYPOINT
    ).foreach { beType =>
      event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, beType.get().asInstanceOf[BlockEntityType[_]], EnergyCapabilityProvider.asInstanceOf)
    }
  }


  object EnergyCapabilityProvider extends ICapabilityProvider[IEnergyStorage, Direction, EnergyStorageImpl] {
    override def getCapability(be: IEnergyStorage, side: Direction): EnergyStorageImpl = {
      be match {
        case pa: PowerAcceptor if pa.canConnectPower(side) => new EnergyStorageImpl(pa, side)
        case _ => null
      }
    }
  }

  def canCharge(stack: ItemStack): Boolean =
    Option(stack.getCapability(Capabilities.EnergyStorage.ITEM)).exists(_.canReceive)

  def charge(stack: ItemStack, amount: Double, simulate: Boolean): Double =
    Option(stack.getCapability(Capabilities.EnergyStorage.ITEM)) match {
      case Some(storage) => amount - Power.fromRF(storage.receiveEnergy(Power.toRF(amount), simulate))
      case _ => amount
    }

  class EnergyStorageImpl(val tile: PowerAcceptor, val side: Direction) extends IEnergyStorage {

    override def getEnergyStored: Int = Power.toRF(tile.globalBuffer(side))

    override def getMaxEnergyStored: Int = Power.toRF(tile.globalBufferSize(side))

    override def canReceive: Boolean = tile.canConnectPower(side)

    override def receiveEnergy(maxReceive: Int, simulate: Boolean): Int =
      Power.toRF(tile.tryChangeBuffer(side, Power.fromRF(maxReceive), !simulate))

    override def canExtract: Boolean = false

    override def extractEnergy(maxExtract: Int, simulate: Boolean): Int = 0
  }
}
