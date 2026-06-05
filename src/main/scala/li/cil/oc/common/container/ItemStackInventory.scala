package li.cil.oc.common.container

import li.cil.oc.util.ClientAccessHelper
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.server.ServerLifecycleHooks

trait ItemStackInventory extends Inventory {
  // The item stack that provides the inventory.
  def container: ItemStack

  private lazy val inventory = Array.fill[ItemStack](getContainerSize)(ItemStack.EMPTY)

  override def items = inventory

  private def provider = if( FMLEnvironment.dist == Dist.CLIENT) ClientAccessHelper.getClientRegistryAccess else ServerLifecycleHooks.getCurrentServer.registryAccess()

  // Initialize the list automatically if we have a container.
  {
    val _container = container
    if (_container != null && !_container.isEmpty) {
      reinitialize()
    }
  }

  // Load items from tag.
  def reinitialize(): Unit = {
    for (i <- items.indices) {
      updateItems(i, ItemStack.EMPTY)
    }
    loadData(container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe, provider)
  }

  // Write items back to tag.
  override def setChanged(): Unit = {
    saveData(container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe, provider)
  }
}
