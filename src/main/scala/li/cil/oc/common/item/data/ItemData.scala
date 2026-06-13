package li.cil.oc.common.item.data

import li.cil.oc.api
import li.cil.oc.api.Persistable
import li.cil.oc.util.ClientAccessHelper
import net.minecraft.client.Minecraft
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.server.ServerLifecycleHooks

abstract class ItemData(val itemName: String) extends Persistable {
  def loadData(stack: ItemStack, provider: HolderLookup.Provider): Unit = {
    val data = stack.get(DataComponents.CUSTOM_DATA)
    if (data != null && !data.isEmpty) {
      loadData(data.copyTag(), provider)
    }
  }

  def saveData(stack: ItemStack, provider: HolderLookup.Provider): Unit = {
    stack.update(
      DataComponents.CUSTOM_DATA,
      CustomData.EMPTY,
      (existing: CustomData) => {
        val tag = existing.copyTag()
        saveData(tag, provider)
        CustomData.of(tag)
      }
    )
  }
  
  def loadData(stack: ItemStack): Unit = {
    loadData(stack, ItemData.defaultProvider)
  }

  def saveData(stack: ItemStack): Unit = {
    saveData(stack, ItemData.defaultProvider)
  }

  def loadData(tag: CompoundTag): Unit = {
    loadData(tag, ItemData.defaultProvider)
  }

  def saveData(tag: CompoundTag): Unit = {
    saveData(tag, ItemData.defaultProvider)
  }

  def createItemStack(provider: HolderLookup.Provider = ItemData.defaultProvider) = {
    if (itemName == null) ItemStack.EMPTY
    else {
      val stack = api.Items.get(itemName).createItemStack(1)
      saveData(stack, provider)
      stack
    }
  }
}

object ItemData {
  def isOnRenderThread = Minecraft.getInstance().isSameThread

  def defaultProvider = if (FMLEnvironment.dist.isClient && isOnRenderThread) {
    ClientAccessHelper.getClientRegistryAccess
  } else {
    ServerLifecycleHooks.getCurrentServer.registryAccess() match {
      case null => FMLEnvironment.dist match {
        case Dist.CLIENT => ClientAccessHelper.getClientRegistryAccess
        case Dist.DEDICATED_SERVER => throw new IllegalStateException("cannot get registry provider before server is initialized!")
      }
      case notNull => notNull
    }
  }
}
