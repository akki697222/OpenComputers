package li.cil.oc.common.recipe

import java.util.UUID
import li.cil.oc.Constants
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.ImmutableItemStack
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.common.datacomponents.OCComponents
import li.cil.oc.common.item.data.DroneData
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.common.item.data.RobotData
import li.cil.oc.common.item.data.TabletData
import li.cil.oc.server.machine.luac.LuaStateFactory
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ExtendedDataComponentHolder._
import li.cil.oc.util.ExtendedItemStack._
import li.cil.oc.util.{ItemUtils, SideTracker}
import net.minecraft.core.component.DataComponents
import net.minecraft.core.{HolderLookup, RegistryAccess}
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.{CompoundTag, StringTag}
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.crafting.{CraftingInput, Recipe}
import net.neoforged.neoforge.server.ServerLifecycleHooks

import java.nio.ByteBuffer
import scala.collection.convert.ImplicitConversionsToScala._
import scala.util.control.Breaks._

object ExtendedRecipe {
  private lazy val drone = api.Items.get(Constants.ItemName.Drone)
  private lazy val eeprom = api.Items.get(Constants.ItemName.EEPROM)
  private lazy val luaBios = api.Items.get(Constants.ItemName.LuaBios)
  private lazy val mcu = api.Items.get(Constants.BlockName.Microcontroller)
  private lazy val navigationUpgrade = api.Items.get(Constants.ItemName.NavigationUpgrade)
  private lazy val linkedCard = api.Items.get(Constants.ItemName.LinkedCard)
  private lazy val floppy = api.Items.get(Constants.ItemName.Floppy)
  private lazy val hdds = Array(
    api.Items.get(Constants.ItemName.HDDTier1),
    api.Items.get(Constants.ItemName.HDDTier2),
    api.Items.get(Constants.ItemName.HDDTier3)
  )
  private lazy val cpus = Array(
    api.Items.get(Constants.ItemName.CPUTier1),
    api.Items.get(Constants.ItemName.CPUTier2),
    api.Items.get(Constants.ItemName.CPUTier3),
    api.Items.get(Constants.ItemName.APUTier1),
    api.Items.get(Constants.ItemName.APUTier2)
  )
  private lazy val robot = api.Items.get(Constants.BlockName.Robot)
  private lazy val tablet = api.Items.get(Constants.ItemName.Tablet)
  private lazy val print = api.Items.get(Constants.BlockName.Print)
  private val beaconBlocks = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "beacon_base_blocks"))
  
  def patchRecipe[R <: Recipe[_]](recipe: R): R = {
    val resultStack = recipe.getResultItem(ServerLifecycleHooks.getCurrentServer.registryAccess())
    val resultItemName = api.Items.get(resultStack)

    val tag = ItemUtils.getTag(resultStack)
    // EEPROM initialization.
    if (resultItemName == eeprom &&
      resultStack.getCount == 1 && tag != null &&
      recipe.getIngredients.size == 2) {
      val nbt = tag.getCompound(Settings.namespace + "data")
      // Load EEPROM code (if it's a string)
      val codeNbt = nbt.get(Settings.namespace + "eeprom")
      if (codeNbt != null && codeNbt.getType == StringTag.TYPE) {
        val codePath = codeNbt.asInstanceOf[StringTag].getAsString
        val code = new Array[Byte](Settings.get.eepromSize)
        val count = OpenComputers.getClass.getResourceAsStream(Settings.scriptPath + codePath).read(code)
        resultStack.set(OCComponents.EEPROM_CODE, ByteBuffer.wrap(code.take(count)))
      }
      // Load EEPROM data (if it's a string)
      val dataNbt = nbt.get(Settings.namespace + "userdata")
      if (dataNbt != null && dataNbt.getType == StringTag.TYPE) {
        val dataPath = dataNbt.asInstanceOf[StringTag].getAsString
        val data = new Array[Byte](Settings.get.eepromDataSize)
        val count = OpenComputers.getClass.getResourceAsStream(Settings.scriptPath + dataPath).read(data)
        resultStack.set(OCComponents.EEPROM_DATA, ByteBuffer.wrap(data.take(count)))
      }
    }

    recipe
  }

  def addNBTToResult(recipe: Recipe[_], craftedStack: ItemStack, inventory: CraftingInput, provider: HolderLookup.Provider): ItemStack = {
    val craftedItemName = api.Items.get(craftedStack)

    if (craftedItemName == navigationUpgrade) {
      for (stack <- getItems(inventory)) {
        if (stack.getItem == Items.FILLED_MAP) {
          // Store information of the map used for crafting in the result.
          craftedStack.setComponent(OCComponents.SOURCE_MAP_ITEM, ImmutableItemStack.copyOf(stack))
        }
      }
    }

    if (craftedItemName == linkedCard) {
      if (SideTracker.isServer) {
        craftedStack.setComponent(OCComponents.TUNNEL, UUID.randomUUID().toString)
      }
    }

    if (cpus.contains(craftedItemName)) {
      LuaStateFactory.setDefaultArch(craftedStack)
    }

    if (craftedItemName == floppy || hdds.contains(craftedItemName)) {
      CustomData.update(DataComponents.CUSTOM_DATA, craftedStack, nbt => {
        if (recipe.canCraftInDimensions(1, 1)) {
          // Formatting / loot to normal disk conversion, only keep coloring.
          val colorKey = Settings.namespace + "color"
          for (stack <- getItems(inventory)) {
            val oldData = ItemUtils.getTag(stack)
            if (api.Items.get(stack) != null && (api.Items.get(stack) == floppy || api.Items.get(stack).name == "lootDisk") && oldData != null) {
              if (oldData.contains(colorKey) && oldData.getInt(colorKey) != DyeColor.LIGHT_GRAY.getId) {
                nbt.put(colorKey, oldData.get(colorKey).copy())
              }
            }
          }
        }
        else if (getItems(inventory).forall(api.Items.get(_) == floppy)) {
          // Copy operation.
          for (stack <- getItems(inventory)) {
            val oldData = ItemUtils.getTag(stack)
            if (api.Items.get(stack) == floppy && oldData != null) {
              for (oldTagName <- oldData.getAllKeys.map(_.asInstanceOf[String]) if !nbt.contains(oldTagName)) {
                nbt.put(oldTagName, oldData.get(oldTagName).copy())
              }
            }
          }
        }
      })
    }

    if (craftedItemName == print &&
      recipe.getIngredients.size == 2) {
      // First, copy old data.
      val data = new PrintData(craftedStack)
      val inputs = getItems(inventory)
      for (stack <- inputs) {
        if (api.Items.get(stack) == print) {
          data.loadData(stack, provider)
        }
      }

      // Then apply new data.
      val glowstoneDust = new ItemStack(Items.GLOWSTONE_DUST)
      val glowstone = new ItemStack(Blocks.GLOWSTONE)
      for (stack <- inputs) {
        if (stack.is(beaconBlocks)) {
          if (data.isBeaconBase) {
            // Crafting wouldn't change anything, prevent accidental resource loss.
            return ItemStack.EMPTY
          }
          data.isBeaconBase = true
        }
        if (ItemStack.isSameItem(glowstoneDust, stack)) {
          if (data.lightLevel == 15) {
            // Crafting wouldn't change anything, prevent accidental resource loss.
            return ItemStack.EMPTY
          }
          data.lightLevel = math.min(15, data.lightLevel + 1)
        }
        if (ItemStack.isSameItem(glowstone, stack)) {
          if (data.lightLevel == 15) {
            // Crafting wouldn't change anything, prevent accidental resource loss.
            return ItemStack.EMPTY
          }
          data.lightLevel = math.min(15, data.lightLevel + 4)
        }
      }

      // Finally apply modified data.
      data.saveData(craftedStack, provider)
    }

    // EEPROM copying.
    if (craftedItemName == eeprom &&
      craftedStack.getCount == 2 &&
      recipe.getIngredients.size == 2) breakable {
      for (stack <- getItems(inventory)) {
        val copy = ItemUtils.getTag(stack)
        if (api.Items.get(stack) == eeprom && copy != null) {
          // Erase node address, just in case.
          copy.getCompound(Settings.namespace + "data").getCompound("node").remove("address")
          CustomData.set(DataComponents.CUSTOM_DATA, craftedStack, copy)
          break()
        }
      }
    }

    // Swapping EEPROM in devices.
    recraft(provider, craftedStack, inventory, mcu, stack => new MCUDataWrapper(stack))
    recraft(provider, craftedStack, inventory, drone, stack => new DroneDataWrapper(stack))
    recraft(provider, craftedStack, inventory, robot, stack => new RobotDataWrapper(stack))
    recraft(provider, craftedStack, inventory, tablet, stack => new TabletDataWrapper(stack))

    craftedStack
  }

  private def getItems(inventory: CraftingInput) = (0 until inventory.size).map(inventory.getItem).filter(!_.isEmpty)

  private def recraft(provider: HolderLookup.Provider, craftedStack: ItemStack, inventory: CraftingInput, descriptor: ItemInfo, dataFactory: (ItemStack) => ItemDataWrapper): Unit = {
    if (api.Items.get(craftedStack) == descriptor) {
      // Find old Microcontroller.
      getItems(inventory).find(api.Items.get(_) == descriptor) match {
        case Some(oldMcu) =>
          val data = dataFactory(oldMcu)

          // Remove old EEPROM.
          val oldRom = data.components.filter(api.Items.get(_) == eeprom)
          data.components = data.components.diff(oldRom)

          // Insert new EEPROM.
          for (stack <- getItems(inventory)) {
            if (api.Items.get(stack) == eeprom) {
              data.components :+= stack.copy.split(1)
            }
          }

          data.save(craftedStack, provider)
        case _ =>
      }
    }
  }

  private trait ItemDataWrapper {
    def components: Array[ItemStack]

    def components_=(value: Array[ItemStack]): Unit

    def save(stack: ItemStack, provider: HolderLookup.Provider): Unit
  }

  private class MCUDataWrapper(val stack: ItemStack) extends ItemDataWrapper {
    private val data = new MicrocontrollerData(stack)

    override def components: Array[ItemStack] = data.components

    override def components_=(value: Array[ItemStack]): Unit = data.components = value

    override def save(stack: ItemStack, provider: HolderLookup.Provider): Unit = data.saveData(stack, provider)
  }

  private class DroneDataWrapper(val stack: ItemStack) extends ItemDataWrapper {
    private val data = new DroneData(stack)

    override def components: Array[ItemStack] = data.components

    override def components_=(value: Array[ItemStack]): Unit = data.components = value

    override def save(stack: ItemStack, provider: HolderLookup.Provider): Unit = data.saveData(stack, provider)
  }

  private class RobotDataWrapper(val stack: ItemStack) extends ItemDataWrapper {
    private val data = new RobotData(stack)

    override def components: Array[ItemStack] = data.components

    override def components_=(value: Array[ItemStack]): Unit = data.components = value

    override def save(stack: ItemStack, provider: HolderLookup.Provider): Unit = data.saveData(stack, provider)
  }

  private class TabletDataWrapper(val stack: ItemStack) extends ItemDataWrapper {
    private val data = new TabletData(stack)

    private var _components: Array[ItemStack] = data.items.filter(!_.isEmpty)

    override def components: Array[ItemStack] = _components

    override def components_=(value: Array[ItemStack]): Unit = {
      _components = value
    }

    override def save(stack: ItemStack, provider: HolderLookup.Provider): Unit = {
      data.items = _components.clone()
      data.saveData(stack, provider)
    }
  }

}
