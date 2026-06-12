package li.cil.oc.integration.minecraftforge

import li.cil.oc.api
import li.cil.oc.integration.Mod
import li.cil.oc.integration.ModProxy
import li.cil.oc.integration.Mods
import net.neoforged.neoforge.common.NeoForge

object ModMinecraftForge extends ModProxy {
  override def getMod: Mod = Mods.Forge

  override def initialize(): Unit = {
    NeoForge.EVENT_BUS.register(EventHandlerMinecraftForge)
    api.IMC.registerItemCharge("MinecraftForge",
      "li.cil.oc.integration.minecraftforge.EventHandlerMinecraftForge.canCharge",
      "li.cil.oc.integration.minecraftforge.EventHandlerMinecraftForge.charge")
    api.Driver.add(DriverEnergyStorage)
  }
}
