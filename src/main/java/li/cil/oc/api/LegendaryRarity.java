package li.cil.oc.api;

import li.cil.oc.util.RarityExt;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows grabbing OpenComputers shiny Legendary rarity.
 */
@ApiStatus.AvailableSince("1.9")
public final class LegendaryRarity {
    private LegendaryRarity() {}

    @ApiStatus.AvailableSince("1.9")
    public static Rarity get() {
        return RarityExt.LEGENDARY.getValue();
    }
}
