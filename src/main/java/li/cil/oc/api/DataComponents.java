package li.cil.oc.api;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.lang.reflect.InvocationTargetException;

public class DataComponents {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> NODE;

    static {
        try {
            //noinspection unchecked
            NODE = (DeferredHolder<DataComponentType<?>, DataComponentType<String>>) DataComponents.class.getClassLoader()
                    .loadClass("li.cil.oc.common.datacomponents.OCComponents")
                    .getMethod("NODE").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
