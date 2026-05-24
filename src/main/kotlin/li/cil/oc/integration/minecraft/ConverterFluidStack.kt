package li.cil.oc.integration.minecraft

import li.cil.oc.api
import net.minecraftforge.registries.ForgeRegistries

object ConverterFluidStack : api.driver.Converter {
    override fun convert(value: Any, output: MutableMap<Any, Any>) {
        if (value is net.minecraftforge.fluids.FluidStack) {
            output["amount"] = value.amount
            output["hasTag"] = value.hasTag()
            val fluid = value.fluid
            val registryName = ForgeRegistries.FLUIDS.getKey(fluid).toString()
            output["name"] = registryName
            output["label"] = fluid.fluidType.getDescription(value).string
        }
    }
}
