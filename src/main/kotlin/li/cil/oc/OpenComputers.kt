package li.cil.oc

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(OpenComputers.ID)
class OpenComputers {
    companion object {
        const val ID = "opencomputers"
        const val Name = "OpenComputers"
        const val McVersion = "1.20.1-forge"
        const val Version = "1.9.0-beta"
        @JvmField val log: Logger = LogManager.getLogger(Name)
        //@JvmField val proxy: Proxy by lazy {
        //    val dist = Environment.get().getDist()
        //    val cls = when (dist) {
        //        Dist.CLIENT -> Class.forName("li.cil.oc.client.Proxy")
        //        else -> Class.forName("li.cil.oc.common.Proxy")
        //    }
        //    cls.getConstructor().newInstance() as Proxy
        //}
    }

    val modContainer: ModContainer = ModLoadingContext.get().getActiveContainer()
    val modBus: IEventBus = FMLJavaModLoadingContext.get().modEventBus
    
    val channel: SimpleChannel? = null
    
    init {
        modBus.register(this)
        /*
        Items.init(modBus)
  Blocks.init(modBus)
  CreativeTab.CREATIVE_TABS.register(modBus)
  TileEntityTypes.init(modBus)
  Recipes.init(modBus)
  LootFunctions.init(modBus)
  EntityTypes.ENTITY_TYPES.register(modBus)
  MenuTypes.MENU_TYPES.register(modBus)
  modBus.register(classOf[Capabilities])
  modBus.register(li.cil.oc.data.DataGenerators)
  modBus.register(CreativeTab)
  OpenComputers.instance = Some(this)
  MinecraftForge.EVENT_BUS.register(OpenComputers.proxy)
  modBus.register(OpenComputers.proxy)
  Settings.load(FMLPaths.CONFIGDIR.get().resolve(Paths.get("opencomputers", "settings.conf")).toFile())
  OpenComputers.proxy.preInit()
  MinecraftForge.EVENT_BUS.register(ThreadPoolFactory)
  Mods.preInit()
         */
    }
}