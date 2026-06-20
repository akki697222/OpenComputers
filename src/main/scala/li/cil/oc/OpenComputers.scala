package li.cil.oc

import li.cil.oc.common.{IMC, Proxy}
import li.cil.oc.common.block.ChameliumBlock
import li.cil.oc.common.blockentity.BlockEntityTypes
import li.cil.oc.common.datacomponents.OCComponents
import li.cil.oc.common.entity.EntityTypes
import li.cil.oc.common.init.{Blocks, Items}
import li.cil.oc.common.menu.MenuTypes
import li.cil.oc.common.recipe.Recipes
import li.cil.oc.integration.Mods
import li.cil.oc.server.loot.LootFunctions
import li.cil.oc.util.ThreadPoolFactory
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.{IEventBus, SubscribeEvent}
import net.neoforged.fml.{InterModComms, ModContainer}
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.{FMLCommonSetupEvent, InterModProcessEvent}
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforgespi.Environment
import org.apache.logging.log4j.{LogManager, Logger}

import java.nio.file.Paths
import scala.collection.convert.ImplicitConversionsToScala._

object OpenComputers {
  final val ID = "opencomputers"

  final val Name = "OpenComputers"

  final val McVersion = "1.20.1-forge"

  final val Version = "1.9.0-beta"

  final val log: Logger = LogManager.getLogger(Name)

  var proxy: Proxy = _

  private var instance: Option[OpenComputers] = None

  def get = instance match {
    case Some(oc) => oc
    case _ => throw new IllegalStateException("not initialized")
  }
}

@Mod(OpenComputers.ID)
class OpenComputers(modBus: IEventBus, modContainer: ModContainer) {
  OpenComputers.proxy = {
    val cls = Environment.get.getDist match {
      case Dist.CLIENT => Class.forName("li.cil.oc.client.Proxy")
      case _ => Class.forName("li.cil.oc.common.ServerProxy")
    }
    cls.getConstructor(classOf[IEventBus]).newInstance(modBus).asInstanceOf[Proxy]
  }

  Settings.load(FMLPaths.CONFIGDIR.get().resolve(Paths.get("opencomputers", "settings.conf")).toFile())

  modBus.register(this)
  OCComponents.REGISTRAR.register(modBus)
  Items.init(modBus)
  Blocks.init(modBus)
  CreativeTab.CREATIVE_TABS.register(modBus)
  BlockEntityTypes.init(modBus)
  Recipes.init(modBus)
  LootFunctions.init(modBus)
  EntityTypes.ENTITY_TYPES.register(modBus)
  MenuTypes.MENU.register(modBus)
  //modBus.register(classOf[Capabilities])
  modBus.register(li.cil.oc.data.DataGenerators)
  modBus.register(CreativeTab)
  OpenComputers.instance = Some(this)
  //NeoForge.EVENT_BUS.register(OpenComputers.proxy)
  modBus.register(OpenComputers.proxy)
  OpenComputers.proxy.preInit()
  NeoForge.EVENT_BUS.register(ThreadPoolFactory)

  // these used to use @EventBusSubscriber but Scala makes this impossible on NeoForge
  modBus.register(ChameliumBlock)

  Mods.preInit() // Must happen after loading Settings but before registry events are fired.

  @SubscribeEvent
  def imc(e: InterModProcessEvent): Unit = {
    // Technically requires synchronization because IMC.sendTo doesn't check the loading stage.
    e.enqueueWork((() => {
      InterModComms.getMessages(OpenComputers.ID).sequential.iterator.foreach(IMC.handleMessage)
    }): Runnable)
  }

  @SubscribeEvent
  def onCommonSetup(e: FMLCommonSetupEvent): Unit = {
    OpenComputers.proxy.init(e)
  }
}
