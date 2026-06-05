package li.cil.oc.common;

import li.cil.oc.common.blockentity.TileEntityTypes;
import li.cil.oc.common.item.traits.Chargeable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class EventHandlerHelper {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        /*
        event.registerBlockEntity(NeoCapabilities.FluidHandler.BLOCK, TileEntityTypes.ROBOT.get(),
      (be, _) => be match {
        case fh: IFluidHandler => fh
        case _ => null
      })
         */
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                TileEntityTypes.ROBOT.get(),
                (be, ignored) -> be
        );
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof Chargeable chargeable) {
                event.registerItem(
                        Capabilities.EnergyStorage.ITEM,
                        (stack, ignored) -> new Chargeable.Provider(stack, chargeable),
                        item
                );
            }
        });
    }
}
