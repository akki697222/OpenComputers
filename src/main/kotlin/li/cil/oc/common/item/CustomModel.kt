package li.cil.oc.common.item

import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraftforge.client.event.ModelEvent

interface CustomModel {
    @OnlyIn(Dist.CLIENT)
    fun getModelLocation(stack: ItemStack): ModelResourceLocation

    @OnlyIn(Dist.CLIENT)
    fun registerModelLocations() {}

    @OnlyIn(Dist.CLIENT)
    fun bakeModels(event: ModelEvent.RegisterAdditional) {}
}
