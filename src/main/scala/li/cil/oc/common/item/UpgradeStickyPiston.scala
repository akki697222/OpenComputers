package li.cil.oc.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.neoforged.neoforge.common.extensions.IItemExtension


class UpgradeStickyPiston(props: Properties) extends Item(props) with traits.SimpleItem with traits.ItemTier with IItemExtension {
  override protected def tooltipName: Option[String] = Option(unlocalizedName)
}

