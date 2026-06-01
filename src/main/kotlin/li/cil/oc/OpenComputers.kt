package li.cil.oc

import li.cil.oc.common.IMC
import li.cil.oc.common.Proxy
import li.cil.oc.common.capabilities.Capabilities
import li.cil.oc.common.entity.EntityTypes
import li.cil.oc.common.init.Blocks
import li.cil.oc.common.init.Items
import li.cil.oc.common.menu.MenuTypes
import li.cil.oc.common.recipe.Recipes
import li.cil.oc.common.blockentity.TileEntityTypes
import li.cil.oc.integration.Mods
import li.cil.oc.server.loot.LootFunctions
import li.cil.oc.util.ThreadPoolFactory
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.forgespi.Environment
import net.minecraftforge.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Paths

object OpenComputers {
    const val ID = "opencomputers"
    const val Name = "OpenComputers"
    const val McVersion = "1.20.1-forge"
    const val Version = "1.9.0-beta"
    val log: Logger = LogManager.getLogger(Name)

    val proxy: Proxy by lazy {
        val clsName = if (Environment.get().dist == Dist.CLIENT) "li.cil.oc.client.Proxy" else "li.cil.oc.common.Proxy"
        Class.forName(clsName).getConstructor().newInstance() as Proxy
    }

    var channel: SimpleChannel? = null

    private var _instance: OpenComputers? = null

    @JvmStatic
    fun get(): OpenComputers = _instance ?: throw IllegalStateException("not initialized")

    internal fun setInstance(instance: OpenComputers) {
        _instance = instance
    }
}

@Mod(OpenComputers.ID)
class OpenComputers {
    val modContainer: ModContainer = ModLoadingContext.get().activeContainer
    val modBus: IEventBus = FMLJavaModLoadingContext.get().modEventBus

    init {
        modBus.register(this)
        Items.init(modBus)
        Blocks.init(modBus)
        CreativeTab.CREATIVE_TABS.register(modBus)
        TileEntityTypes.init(modBus)
        Recipes.init(modBus)
        LootFunctions.init(modBus)
        EntityTypes.ENTITY_TYPES.register(modBus)
        MenuTypes.MENU_TYPES.register(modBus)
        modBus.register(Capabilities::class.java)
        modBus.register(CreativeTab)
        OpenComputers.setInstance(this)
        MinecraftForge.EVENT_BUS.register(OpenComputers.proxy)
        modBus.register(OpenComputers.proxy)
        Settings.load(FMLPaths.CONFIGDIR.get().resolve(Paths.get("opencomputers", "settings.conf")).toFile())
        OpenComputers.proxy.preInit()
        MinecraftForge.EVENT_BUS.register(ThreadPoolFactory)
        Mods.preInit() // Must happen after loading Settings but before registry events are fired.
    }

    @SubscribeEvent
    fun imc(e: InterModProcessEvent) {
        // Technically requires synchronization because IMC.sendTo doesn't check the loading stage.
        e.enqueueWork {
            InterModComms.getMessages(OpenComputers.ID).forEach { IMC.handleMessage(it) }
        }
    }

    @SubscribeEvent
    fun onCommonSetup(e: FMLCommonSetupEvent) {
        OpenComputers.proxy.init(e)
    }
}
