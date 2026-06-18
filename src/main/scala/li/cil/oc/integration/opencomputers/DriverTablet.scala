package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.network.{Component, EnvironmentHost, ManagedEnvironment, Visibility}
import li.cil.oc.common.Slot
import li.cil.oc.common.item.Tablet
import li.cil.oc.common.item.data.TabletData
import li.cil.oc.util.ItemUtils
import net.minecraft.core.component.DataComponents
import li.cil.oc.util.ExtendedItemStack._
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.{CompoundTag, Tag}
import net.minecraft.world.item.component.CustomData

import java.util.function.Consumer

object DriverTablet extends Item {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.Tablet))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment =
    if (host.getEnvironmentLevel != null && host.getEnvironmentLevel.isClientSide) null
    else {
      Tablet.Server.cache.invalidate(Tablet.getOrCreateId(stack))
      val data = new TabletData(stack)
      data.items.collectFirst {
        case fs if !fs.isEmpty && DriverFileSystem.worksWith(fs) => fs
      }.map(DriverFileSystem.createEnvironment(_, host)) match {
        case Some(environment) => environment.node match {
          case component: Component =>
            component.setVisibility(Visibility.Network)
            environment.saveData(stack)
            environment
          case _ => null
        }
        case _ => null
      }
    }

  override def slot(stack: ItemStack) = Slot.Tablet

  def mapToDataTag(stack: ItemStack, tag: CompoundTag): CompoundTag = {
    val data = new TabletData(stack)
    val index = data.items.indexWhere {
      case fs if !fs.isEmpty => DriverFileSystem.worksWith(fs)
      case _ => false
    }
    if (index >= 0 && tag != null && tag.contains(Settings.namespace + "items")) {
      val baseTag = tag.getList(Settings.namespace + "items", Tag.TAG_COMPOUND).getCompound(index)
      if (!baseTag.contains("item")) {
        baseTag.put("item", new CompoundTag())
      }
      val itemTag = baseTag.getCompound("item")
      if (!itemTag.contains("tag")) {
        itemTag.put("tag", new CompoundTag())
      }
      val stackTag = itemTag.getCompound("tag")
      if (!stackTag.contains(Settings.namespace + "data")) {
        stackTag.put(Settings.namespace + "data", new CompoundTag())
      }
      stackTag.getCompound(Settings.namespace + "data")
    }
    else new CompoundTag()
  }
}
