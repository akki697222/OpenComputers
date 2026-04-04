package li.cil.oc.api.prefab;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.oc.api.manual.TabIconRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 * In 1.18, this class isn't required
 */
@SuppressWarnings("UnusedDeclaration")
@Deprecated(forRemoval = true)
public class ItemStackTabIconRenderer implements TabIconRenderer {
    private final ItemStack stack;

    public ItemStackTabIconRenderer(ItemStack stack) {
        this.stack = stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack matrix) {
        com.mojang.math.Vector4f vec = new com.mojang.math.Vector4f(0, 0, 0, 1);
        vec.transform(matrix.last().pose());
        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, (int) vec.x(), (int) vec.y());
    }
}
