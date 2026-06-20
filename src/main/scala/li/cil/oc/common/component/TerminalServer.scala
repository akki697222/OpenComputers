package li.cil.oc.common.component

import java.util
import java.util.UUID
import li.cil.oc.Constants
import li.cil.oc.api.driver.DeviceInfo.DeviceAttribute
import li.cil.oc.api.driver.DeviceInfo.DeviceClass
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.component.RackBusConnectable
import li.cil.oc.api.component.RackMountable
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.internal.Keyboard.UsabilityChecker
import li.cil.oc.api.network.Analyzable
import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.network.Message
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.util.Lifecycle
import li.cil.oc.api.util.StateAware
import li.cil.oc.api.util.StateAware.State
import li.cil.oc.common.Tier
import li.cil.oc.common.datacomponents.{OCComponents, TerminalReference}
import li.cil.oc.common.item
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ExtendedDataComponentHolder._
import net.minecraft.core.component.{DataComponentHolder, DataComponents}
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StringTag
import net.minecraft.core.{Direction, HolderLookup}

import scala.collection.convert.ImplicitConversionsToScala._
import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.mutable
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.Tag
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.component.CustomData
import net.neoforged.neoforge.common.MutableDataComponentHolder

class TerminalServer(val rack: api.internal.Rack, val slot: Int) extends Environment with EnvironmentHost with Analyzable with RackMountable with Lifecycle with DeviceInfo {
  val node = api.Network.newNode(this, Visibility.None).create()

  lazy val buffer = {
    val screenItem = api.Items.get(Constants.BlockName.ScreenTier1).createItemStack(1)
    val buffer = api.Driver.driverFor(screenItem, getClass).createEnvironment(screenItem, this).asInstanceOf[api.internal.TextBuffer]
    val (maxWidth, maxHeight) = Settings.screenResolutionsByTier(Tier.Four)
    buffer.setMaximumResolution(maxWidth, maxHeight)
    buffer.setMaximumColorDepth(Settings.screenDepthsByTier(Tier.Four))
    buffer
  }

  lazy val keyboard = {
    val keyboardItem = api.Items.get(Constants.BlockName.Keyboard).createItemStack(1)
    val keyboard = api.Driver.driverFor(keyboardItem, getClass).createEnvironment(keyboardItem, this).asInstanceOf[api.internal.Keyboard]
    keyboard.setUsableOverride((keyboard: api.internal.Keyboard, player: Player) => {
      val stack = player.getItemInHand(InteractionHand.MAIN_HAND)
      stack.getItem match {
        case t: item.Terminal if stack.has(OCComponents.TERMINAL_REFERENCE) =>
          sidedKeys.contains(stack.getComponent(OCComponents.TERMINAL_REFERENCE).get.key)
        case _ => false
      }
    })
    keyboard
  }

  var range = Settings.get.maxWirelessRange(Tier.Two)
  val keys = mutable.ListBuffer.empty[String]

  def hasAddress: Boolean = {
    if (rack != null) {
      val data = rack.getMountableData(slot)
      if (data != null) {
        return data.has(OCComponents.ADDRESS)
      }
    }
    false
  }

  def address: String = rack.getMountableData(slot).getComponent(OCComponents.ADDRESS).get

  def sidedKeys = {
    if (!rack.getEnvironmentLevel.isClientSide) keys
    else rack.getMountableData(slot).getComponent(OCComponents.KEYS) getOrElse List.empty
  }

  // ----------------------------------------------------------------------- //
  // DeviceInfo

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Terminal server",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "RemoteViewing EX"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //
  // Environment

  override def onConnect(node: Node): Unit = {
    if (node == this.node) {
      node.connect(buffer.node)
      node.connect(keyboard.node)
      buffer.node.connect(keyboard.node)
    }
  }

  override def onDisconnect(node: Node): Unit = {
    if (node == this.node) {
      buffer.node.remove()
      keyboard.node.remove()
    }
  }

  override def onMessage(message: Message): Unit = {
  }

  // ----------------------------------------------------------------------- //
  // EnvironmentHost

  override def getEnvironmentLevel = rack.getEnvironmentLevel

  override def xPosition = rack.xPosition

  override def yPosition = rack.yPosition

  override def zPosition = rack.zPosition

  override def markChanged() = rack.markChanged()

  // ----------------------------------------------------------------------- //
  // RackMountable

  override def describeForClient(holder: MutableDataComponentHolder): Unit = {
    if (node.address == null) api.Network.joinNewNetwork(node)

    holder.setComponent(OCComponents.KEYS, keys.toList)
    holder.setComponent(OCComponents.ADDRESS, node.address)
  }

  override def getConnectableCount: Int = 0

  override def getConnectableAt(index: Int): RackBusConnectable = null

  override def onActivate(player: Player, hand: InteractionHand, heldItem: ItemStack, hitX: Float, hitY: Float): Boolean = {
    if (api.Items.get(heldItem) == api.Items.get(Constants.ItemName.Terminal)) {
      if (!getEnvironmentLevel.isClientSide) {
        val key = UUID.randomUUID().toString
        
        for(component <- heldItem.getComponent(OCComponents.TERMINAL_REFERENCE)) {
          keys -= component.key
        }
        
        val maxSize = Settings.get.terminalsPerServer
        while (keys.length >= maxSize) {
          keys.remove(0)
        }
        keys += key
        heldItem.setComponent(OCComponents.TERMINAL_REFERENCE, TerminalReference(key, node.address))
        rack.markChanged(slot)
        player.getInventory.setChanged()
      }
      true
    }
    else false
  }

  // ----------------------------------------------------------------------- //
  // Persistable

  private final val BufferTag = Settings.namespace + "buffer"
  private final val KeyboardTag = Settings.namespace + "keyboard"
  private final val KeysTag = Settings.namespace + "keys"

  override def loadData(holder: DataComponentHolder): Unit = {
    if (!rack.getEnvironmentLevel.isClientSide) {
      node.loadData(holder)
    }
    buffer.loadData(holder)
    keyboard.loadData(holder)
    keys.clear()
    keys ++= holder.getOrDefault(OCComponents.KEYS, List.empty)
  }

  override def saveData(holder: MutableDataComponentHolder): Unit = {
    node.saveData(holder)
    buffer.saveData(holder)
    keyboard.saveData(holder)
    holder.set(OCComponents.KEYS, keys.toList)
  }

  // ----------------------------------------------------------------------- //
  // ManagedEnvironment

  override def canUpdate: Boolean = true

  override def update(): Unit = {
    if (getEnvironmentLevel.isClientSide || (node.address != null && node.network != null)) {
      buffer.update()
    }
  }

  // ----------------------------------------------------------------------- //
  // StateAware

  override def getCurrentState: util.EnumSet[State] = {
    util.EnumSet.noneOf(classOf[StateAware.State])
  }

  // ----------------------------------------------------------------------- //
  // Analyzable

  override def onAnalyze(player: Player, side: Direction, hitX: Float, hitY: Float, hitZ: Float) = Array(buffer.node, keyboard.node)

  // ----------------------------------------------------------------------- //
  // LifeCycle

  override def onLifecycleStateChange(state: Lifecycle.LifecycleState): Unit = if (rack.getEnvironmentLevel.isClientSide) state match {
    case Lifecycle.LifecycleState.Initialized =>
      TerminalServer.loaded.add(this)
    case Lifecycle.LifecycleState.Disposed =>
      TerminalServer.loaded.remove(this)
    case _ => // Ignore.
  }
}

object TerminalServer {
  val loaded = new TerminalServerCache()

  // we need a smart cache because nodes are loaded in before they have addresses
  // and we need a unique set of terminal servers based on address
  // This cache acts as a Map[address: String, term: TerminalServer]
  // But it can store terminals before they have an address
  // Null-address terminals are not available for binding
  // As an address loads, repeated addresses are dropped from the list
  class TerminalServerCache {

    private val ready: mutable.Map[String, TerminalServer] = new mutable.HashMap[String, TerminalServer]()
    private val pending: mutable.Buffer[TerminalServer] = mutable.Buffer.empty[TerminalServer]

    private def completePending(): Unit = {
      val promoted: mutable.Buffer[TerminalServer] = mutable.Buffer.empty[TerminalServer]
      pending.foreach { term => if (term.hasAddress)
        promoted += term
      }
      promoted.foreach { term =>
        pending -= term
        val address = term.address
        if (!ready.contains(address)) {
          ready.put(address, term)
        }
      }
    }

    def add(terminal: TerminalServer): Boolean = {
      completePending()
      if (terminal.hasAddress) {
        val newAddress: String = terminal.address
        if (ready.contains(newAddress)) {
          false
        } else {
          ready.put(newAddress, terminal)
          true
        }
      }
      else {
        pending += terminal
        true
      }
    }

    def remove(terminal: TerminalServer): Boolean = {
      completePending()
      if (terminal.hasAddress)
        ready.remove(terminal.address).isDefined
      else {
        val before = pending.size
        pending -= terminal
        pending.size > before
      }
    }

    def clear(): Unit = {
      ready.clear()
      pending.clear()
    }

    def find(address: String): Option[TerminalServer] = {
      completePending()
      ready.getOrDefault(address, null) match {
        case term: TerminalServer => Option(term)
        case _ => None
      }
    }
  }
}
