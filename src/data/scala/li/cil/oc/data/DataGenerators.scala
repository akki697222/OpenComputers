package li.cil.oc.data

import java.util
import java.util.function.Consumer
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.advancements.AdvancementSubProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.bus.api.SubscribeEvent

object DataGenerators {
  @SubscribeEvent
  def gatherData(event: GatherDataEvent): Unit = {
    val generator = event.getGenerator
    val output = generator.getPackOutput
    val registries = event.getLookupProvider

    generator.addProvider(
      event.includeServer(),
      new AdvancementProvider(
        output,
        registries,
        util.List.of[AdvancementSubProvider](new AdvancementSubProvider {
          override def generate(registries: HolderLookup.Provider, writer: Consumer[AdvancementHolder]): Unit = {
            Advancements.generate(registries, writer)
          }
        })
      )
    )
  }
}