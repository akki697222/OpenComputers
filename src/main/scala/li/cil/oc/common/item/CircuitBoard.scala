package li.cil.oc.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.neoforged.neoforge.common.extensions.IItemExtension


class CircuitBoard(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension
