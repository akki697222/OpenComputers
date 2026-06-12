package li.cil.oc.integration.mekanism

import java.util
import li.cil.oc.Settings
import li.cil.oc.api
import mekanism.api.MekanismAPI
import mekanism.api.chemical.ChemicalStack

import scala.collection.convert.ImplicitConversionsToScala._

object ConverterChemicalStack extends api.driver.Converter {
  override def convert(value: scala.Any, output: util.Map[AnyRef, AnyRef]) =
    value match {
      case stack: ChemicalStack =>
        if (Settings.get.insertIdsInConverters) {
          output += "id" -> Int.box(MekanismAPI.CHEMICAL_REGISTRY.getId(stack.getChemical))
        }
        output += "amount" -> Long.box(stack.getAmount)
        val chemical = stack.getChemical
        if (chemical != null) {
          output += "name" -> chemical.getRegistryName.toString
          output += "label" -> chemical.getTextComponent.getString
        }
      case _ =>
    }
}
