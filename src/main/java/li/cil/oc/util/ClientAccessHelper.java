package li.cil.oc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

public final class ClientAccessHelper {
    public static RegistryAccess getClientRegistryAccess() {
        return Minecraft.getInstance().player.level().registryAccess();
    }
    
    private ClientAccessHelper() {}
}
