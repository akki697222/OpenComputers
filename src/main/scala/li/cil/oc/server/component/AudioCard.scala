package li.cil.oc.server.component

import java.io.IOException
import java.util
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api.Network
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, Message, Node, Visibility}
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import li.cil.oc.common.datacomponents.OCComponents
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.ExtendedDataComponentHolder._
import li.cil.oc.server.PacketSender
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentHolder

import scala.collection.mutable
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.IntArrayTag
import net.neoforged.neoforge.common.MutableDataComponentHolder

import scala.jdk.CollectionConverters._

class AudioCard(private val host: EnvironmentHost) extends AbstractManagedEnvironment with DeviceInfo {
  override val node: Node = Network.newNode(this, Visibility.Neighbors)
    .withComponent("audio")
    .withConnector()
    .create()

  private val owners = mutable.Map.empty[String, mutable.Set[Int]]
  private val sessions = mutable.Map.empty[Int, AudioCardSession]
  private var nextHandle = 1

  private def chunkSize: Int = math.max(1, Settings.get.audioCardChunkSize)
  private def bufferLimit: Int = math.max(chunkSize, Settings.get.audioCardBufferLimit)
  private def defaultSampleRate: Int = Settings.get.audioCardSampleRate

  private def hostPos: BlockPosition = BlockPosition(host)

  private def nextId(): Int = synchronized {
    val id = nextHandle
    nextHandle += 1
    id
  }

  private def session(handle: Int): AudioCardSession =
    sessions.getOrElse(handle, throw new IllegalArgumentException("invalid handle"))

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Multimedia,
    DeviceAttribute.Description -> "Audio Streaming Interface",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.ViridiaComputronics,
    DeviceAttribute.Product -> "WaveBlaster Zero"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo.asJava

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function([channel:number, sampleRate:number, mode:string]):userdata -- open an audio buffer handle.
  - channel: output channel index (default: 0)
  - sampleRate: samples per second (default: server config value)
  - mode: PCM format string (default: "mono8")
      "mono8"    -- mono,   8-bit unsigned  (DFPWM output)
      "mono16"   -- mono,   16-bit signed little-endian
      "stereo8"  -- stereo, 8-bit unsigned
      "stereo16" -- stereo, 16-bit signed little-endian (WAV stereo)
  """)
  def open(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    if (owners.get(context.node.address).fold(false)(_.size >= Settings.get.maxHandles)) {
      throw new IOException("too many open handles")
    }
    val channel = args.optInteger(0, 0)
    val sampleRate = args.optInteger(1, defaultSampleRate)
    val mode = args.optString(2, "mono8")
    val handle = nextId()

    sessions(handle) = new AudioCardSession(handle, channel, sampleRate, mode)
    owners.getOrElseUpdate(context.node.address, mutable.Set.empty[Int]) += handle

    result(new AudioHandleValue(node.address, handle))
  }

  @Callback(direct = true, doc = "function(handle:userdata, pcm:string):boolean -- append raw PCM bytes to the handle buffer.")
  def send(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    val data = args.checkByteArray(1)
    checkOwner(context.node.address, handle)

    val s = session(handle)
    if (s.isPlayingNow) return result(null, "already playing")
    if (s.closed) return result(null, "handle closed")
    if (s.size + data.length > bufferLimit) return result(null, "buffer full")

    s.append(data)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- flush buffer to clients and start playback.")
  def play(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    val s = session(handle)

    if (s.closed) return result(null, "handle closed")
    if (s.size == 0) return result(null, "buffer empty")

    s.startPlayback()

    PacketSender.sendAudioStart(host, handle, s.channel, s.sampleRate, s.channels, s.format, s.loop, hostPos)

    val pcm = s.pcm
    var off = 0
    while (off < pcm.length) {
      val len = math.min(chunkSize, pcm.length - off)
      PacketSender.sendAudioChunk(host, handle, pcm.slice(off, off + len))
      off += len
    }

    PacketSender.sendAudioPlay(host, handle)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- pause playback if active.")
  def pause(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    val s = session(handle)
    if (s.closed) return result(null, "handle closed")

    s.pausePlayback()
    PacketSender.sendAudioPause(host, handle)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- resume playback if paused.")
  def resume(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    val s = session(handle)
    if (s.closed) return result(null, "handle closed")

    s.resumePlayback()
    PacketSender.sendAudioResume(host, handle)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- stop playback.")
  def stop(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    val s = session(handle)
    if (s.closed) return result(null, "handle closed")

    s.stopPlayback()
    PacketSender.sendAudioStop(host, handle)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata, loop:boolean):boolean -- set loop mode.")
  def setLoop(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    val loop = args.checkBoolean(1)
    checkOwner(context.node.address, handle)
    val s = session(handle)
    if (s.closed) return result(null, "handle closed")

    s.loop = loop
    PacketSender.sendAudioSetLoop(host, handle, loop)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- close and dispose handle.")
  def close(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    closeHandle(context.node.address, handle)
    result(true)
  }

  @Callback(direct = true, doc = "function(handle:userdata):number -- get current buffer size.")
  def size(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    result(session(handle).size)
  }

  @Callback(direct = true, doc = "function(handle:userdata):boolean -- check if audio is currently playing.")
  def isPlaying(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    val handle = checkHandle(args, 0)
    checkOwner(context.node.address, handle)
    result(session(handle).isPlayingNow)
  }

  // ----------------------------------------------------------------------- //

  def checkHandle(args: Arguments, index: Int): Int = {
    if (args.isInteger(index)) {
      args.checkInteger(index)
    } else if (args.isTable(index)) {
      args.checkTable(index).get("handle") match {
        case handle: Number => handle.intValue()
        case _ => throw new IOException("bad file descriptor")
      }
    } else args.checkAny(index) match {
      case handle: AudioHandleValue => handle.handle
      case _ => throw new IOException("bad file descriptor")
    }
  }

  def closeHandle(owner: String, handle: Int): Unit = {
    sessions.get(handle) match {
      case Some(s) =>
        owners.get(owner) match {
          case Some(set) if set.remove(handle) =>
            PacketSender.sendAudioClose(host, handle)
            s.closed = true
            sessions.remove(handle)
          case _ => throw new IOException("bad file descriptor")
        }
      case None => throw new IOException("bad file descriptor")
    }
  }

  private def checkOwner(owner: String, handle: Int) =
    if (!owners.contains(owner) || !owners(owner).contains(handle))
      throw new IOException("bad file descriptor")

  // ----------------------------------------------------------------------- //

  override def onMessage(message: Message): Unit = synchronized {
    super.onMessage(message)
    if (message.name == "computer.stopped" || message.name == "computer.started") {
      owners.get(message.source.address) match {
        case Some(set) =>
          set.foreach { handle =>
            PacketSender.sendAudioClose(host, handle)
            sessions.get(handle).foreach(_.closed = true)
            sessions.remove(handle)
          }
          set.clear()
        case _ =>
      }
    }
  }

  override def onDisconnect(node: Node): Unit = synchronized {
    super.onDisconnect(node)
    if (node == this.node) {
      sessions.keys.foreach(handle => PacketSender.sendAudioClose(host, handle))
      sessions.clear()
      owners.clear()
    }
    else if (owners.contains(node.address)) {
      for (handle <- owners(node.address)) {
        PacketSender.sendAudioClose(host, handle)
        sessions.get(handle).foreach(_.closed = true)
        sessions.remove(handle)
      }
      owners.remove(node.address)
    }
  }

  // ----------------------------------------------------------------------- //

  override def loadData(holder: DataComponentHolder): Unit = {
    super.loadData(holder)

    for(owners <- holder.getComponent(OCComponents.HANDLES)) {
      this.owners ++= owners.map { case k -> v => k -> v.to(mutable.Set) }
    }
  }

  override def saveData(holder: MutableDataComponentHolder): Unit = {
    super.saveData(holder)

    holder.setComponent(OCComponents.HANDLES, Map.from(owners.map { case k -> v => k -> v.toSet }))
  }
}