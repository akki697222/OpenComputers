package li.cil.oc.util

import li.cil.oc.common.datacomponents.Migrator
import li.cil.oc.common.item.data.ItemData
import net.minecraft.core.component.{DataComponentType, DataComponents}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

import java.util.function.Supplier
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

    def getOrCreateTag: CompoundTag = {
      val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
      if (data.isEmpty) {
        val tag = new CompoundTag()
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
        tag
      } else data.getUnsafe
    }

    def removeTagKey(key: String): Unit = {
      if (stack.has(DataComponents.CUSTOM_DATA)) {
        val tag = stack.get(DataComponents.CUSTOM_DATA).getUnsafe.copy()
        tag.remove(key)
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
      }
    }
  }
}
