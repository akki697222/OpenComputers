package li.cil.oc.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.neoforged.neoforge.common.extensions.IItemExtension


class ArrowKeys(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension {
  override protected def tooltipName = None
}
