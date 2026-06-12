package li.cil.oc.util

import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

/**
  * @author asie, Vexatos
  */
object ItemColorizer {
  /**
    * Return whether the specified armor ItemStack has a color.
    */
  def hasColor(stack: ItemStack): Boolean = stack.hasTag && stack.getTag.contains("display") && stack.getTag.getCompound("display").contains("color")

  /**
    * Return the color for the specified armor ItemStack.
    */
  def getColor(stack: ItemStack): Int = {
    val tag = stack.getTag
    if (tag != null) {
      if (tag.contains("display")) {
        val displayTag = tag.getCompound("display")
        if (displayTag.contains("color")) displayTag.getInt("color") else -1
      }
      else -1
    }
    else -1
  }

  def removeColor(stack: ItemStack): Unit = {
    stack.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY, (existing: net.minecraft.world.item.component.CustomData) => {
      val tag = existing.copyTag()
      if (tag.contains("display")) {
        val displayTag = tag.getCompound("display")
        if (displayTag.contains("color")) displayTag.remove("color")
        if (displayTag.isEmpty) tag.remove("display")
      }
      if (tag.isEmpty) net.minecraft.world.item.component.CustomData.EMPTY
      else net.minecraft.world.item.component.CustomData.of(tag)
    })
  }

  def setColor(stack: ItemStack, color: Int): Unit = {
    stack.getOrCreateTagElement("display").putInt("color", color)
  }
}
