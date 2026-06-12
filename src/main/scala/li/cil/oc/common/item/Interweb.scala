package li.cil.oc.common.item

import net.neoforged.neoforge.common.extensions.IItemExtension
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties


class Interweb(props: Properties) extends Item(props) with traits.SimpleItem with IItemExtension
