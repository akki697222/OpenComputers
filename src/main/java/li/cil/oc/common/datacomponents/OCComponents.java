package li.cil.oc.common.datacomponents;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public final class OCComponents {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "opencomputers");

    public static final Supplier<DataComponentType<DyeColor>> DISK_COLOR = REGISTRAR.registerComponentType(
            "disk_color",
            builder -> builder
                    .persistent(DyeColor.CODEC)
                    .networkSynchronized(DyeColor.STREAM_CODEC)
    );
    
    public static final Supplier<DataComponentType<ResourceLocation>> LOOT_DISK = REGISTRAR.registerComponentType(
            "loot_disk",
            builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<String>> LABEL = REGISTRAR.registerComponentType(
            "label",
            builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final Supplier<DataComponentType<Boolean>> READONLY = REGISTRAR.registerComponentType(
            "readonly",
            builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
    );

    private static Supplier<DataComponentType<ByteBuffer>> bytes(String name) {
        return REGISTRAR.registerComponentType(name, builder -> builder.persistent(Codec.BYTE_BUFFER));
    }

    public static final Supplier<DataComponentType<ByteBuffer>> EEPROM_CODE = bytes("eeprom_code");
    public static final Supplier<DataComponentType<ByteBuffer>> EEPROM_DATA = bytes("eeprom_data");
}
