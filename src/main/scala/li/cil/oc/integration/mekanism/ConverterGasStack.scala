package li.cil.oc.integration.mekanism

import java.util

import li.cil.oc.Settings
import li.cil.oc.api
import mekanism.api.MekanismAPI
import mekanism.api.chemical.Chemical
import mekanism.api.chemical.ChemicalStack

import scala.collection.convert.ImplicitConversionsToScala._

object ConverterGasStack extends api.driver.Converter {
  override def convert(value: scala.Any, output: util.Map[AnyRef, AnyRef]) =
    value match {
      case stack: ChemicalStack =>
        if (Settings.get.insertIdsInConverters) {
          output += "id" -> MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical).toString
        }
        output += "amount" -> Long.box(stack.getAmount)
        val gas = stack.getChemical
        if (gas != null) {
          output += "name" -> MekanismAPI.CHEMICAL_REGISTRY.getKey(gas).toString
          output += "label" -> gas.getTextComponent.getString
        }
      case _ =>
    }
}
