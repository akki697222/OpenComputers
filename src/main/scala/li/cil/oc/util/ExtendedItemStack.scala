package li.cil.oc.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import scala.collection.mutable

object ExtendedItemStack {
  implicit def extendedItemStack(stack: ItemStack): ExtendedItemStack = new ExtendedItemStack(stack)

  class ExtendedItemStack(val stack: ItemStack) {
    def getOrCreateTagElement(key: String): CompoundTag = {
      val customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
      if (customData.contains(key)) {
        customData.getCompound(key)
      } else {
        val tag = new CompoundTag()
        customData.put(key, tag)
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
        tag
      }
    }
    
    def hasTag: Boolean = stack.has(DataComponents.CUSTOM_DATA)
    
    def getTag: CompoundTag = stack.get(DataComponents.CUSTOM_DATA).getUnsafe
    
    
  }
}
