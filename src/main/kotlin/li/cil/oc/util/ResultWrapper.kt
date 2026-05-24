package li.cil.oc.util

import net.minecraft.world.item.ItemStack

object ResultWrapper {
    @JvmField
    val unit: Any = Unit

    @JvmStatic
    fun result(vararg args: Any?): Array<Any?> {
        fun unwrap(arg: Any?): Any? = when (arg) {
            is scala.math.ScalaNumber -> arg.underlying()
            is ItemStack -> if (arg.isEmpty) null else arg
            else -> arg
        }
        return args.map(::unwrap).toTypedArray()
    }
}
