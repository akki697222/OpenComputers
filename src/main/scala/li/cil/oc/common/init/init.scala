package li.cil.oc.common

import net.neoforged.neoforge.registries.DeferredHolder
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.Holder

package object init {
  type RegistryObject[T] = DeferredHolder[_, T]

  implicit class RegistryObjectExtensions[T](val holder: DeferredHolder[_, T]) extends AnyVal {
    def get(): T = holder.value()
    def getId: ResourceLocation = holder.getId
    def asHolder(): Holder[T] = holder.asInstanceOf[Holder[T]]
  }
}