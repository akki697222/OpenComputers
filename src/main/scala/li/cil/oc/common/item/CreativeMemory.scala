package li.cil.oc.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.neoforged.neoforge.common.extensions.IItemExtension


class CreativeMemory(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension {
  @Deprecated
  override def getDescriptionId = super.getDescriptionId

  override protected def tooltipName = Option(unlocalizedName)
}
