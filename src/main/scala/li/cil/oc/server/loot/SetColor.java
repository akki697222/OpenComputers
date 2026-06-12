package li.cil.oc.server.loot;

import java.util.OptionalInt;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import li.cil.oc.util.ItemColorizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public final class SetColor extends LootItemConditionalFunction {
    private static final MapCodec<OptionalInt> COLOR_CODEC = Codec.INT
            .validate(color -> color >= 0 && color <= 0xFFFFFF
                    ? DataResult.success(color)
                    : DataResult.error(() -> "Invalid RGB color: " + color))
            .optionalFieldOf("color")
            .xmap(optional -> optional.map(OptionalInt::of).orElseGet(OptionalInt::empty),
                    color -> color.isPresent() ? java.util.Optional.of(color.getAsInt()) : java.util.Optional.empty());

    public static final MapCodec<SetColor> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .and(COLOR_CODEC.forGetter(setColor -> setColor.color))
                    .apply(instance, SetColor::new)
    );

    private final OptionalInt color;

    private SetColor(List<LootItemCondition> conditions, OptionalInt color) {
        super(conditions);
        this.color = color;
    }

    @Override
    public @NotNull LootItemFunctionType<SetColor> getType() {
        return LootFunctions.SET_COLOR.get();
    }

    @Override
    @NotNull
    public ItemStack run(ItemStack stack, @NotNull LootContext ctx) {
        if (stack.isEmpty()) return stack;
        if (color.isPresent()) {
            ItemColorizer.setColor(stack, color.getAsInt());
        } else {
            ItemColorizer.removeColor(stack);
        }
        return stack;
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private OptionalInt color = OptionalInt.empty();

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }

        public Builder withoutColor() {
            color = OptionalInt.empty();
            return this;
        }

        public Builder withColor(int color) {
            if (color < 0 || color > 0xFFFFFF) throw new IllegalArgumentException("Invalid RGB color: " + color);
            this.color = OptionalInt.of(color);
            return this;
        }

        @Override
        public @NotNull SetColor build() {
            return new SetColor(getConditions(), color);
        }
    }

    public static Builder setColor() {
        return new Builder();
    }

}
