package li.cil.oc.common.event

import li.cil.oc.OpenComputers
import li.cil.oc.api.event.RobotMoveEvent
import li.cil.oc.server.component.UpgradeChunkloader
import li.cil.oc.util.BlockPosition
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.world.chunk.{
  RegisterTicketControllersEvent,
  TicketController,
  TicketHelper
}
import net.neoforged.neoforge.event.level.LevelEvent

import java.util.UUID
import scala.collection.convert.ImplicitConversionsToScala._
import scala.collection.{immutable, mutable}

object ChunkloaderUpgradeHandler {
  private val restoredTickets = mutable.Map.empty[UUID, ChunkPos]
  var ticketController: TicketController = _

  private def parseAddress(addr: String): Option[UUID] = try {
    Some(UUID.fromString(addr))
  } catch {
    case _: RuntimeException => None
  }

  def claimTicket(addr: String) = parseAddress(addr).flatMap(restoredTickets.remove)

  def onRegisterTicketControllers(event: RegisterTicketControllersEvent): Unit = {
    ticketController = new TicketController(
      ResourceLocation.fromNamespaceAndPath(OpenComputers.ID, "chunkloader"),
      (world, helper) => validateTickets(world, helper)
    )
    event.register(ticketController)
  }

  private def validateTickets(world: ServerLevel, helper: TicketHelper): Unit = {
    for ((owner, ticketSet) <- helper.getEntityTickets) {
      restoredTickets += owner -> null
      val tickets = ticketSet.ticking()
      if (tickets.size == 9) {
        var (minX, minZ, maxX, maxZ) = (0, 0, 0, 0)
        for (combinedPos <- tickets) {
          val x = ChunkPos.getX(combinedPos)
          val z = ChunkPos.getZ(combinedPos)
          minX = minX min x
          maxX = maxX max x
          minZ = minZ min z
          maxZ = maxZ max z
        }
        if (minX + 2 == maxX && minZ + 2 == maxZ) {
          val x = minX + 1
          val z = minZ + 1
          OpenComputers.log.info(s"Restoring chunk loader ticket for upgrade at chunk ($x, $z) with address ${owner}.")
          restoredTickets += owner -> new ChunkPos(x, z)
        } else {
          OpenComputers.log.warn(s"Chunk loader ticket for $owner loads an incorrect shape.")
          helper.removeAllTickets(owner)
        }
      } else {
        OpenComputers.log.warn(s"Chunk loader ticket for $owner loads ${tickets.size} chunks.")
        helper.removeAllTickets(owner)
      }
    }
  }

  @SubscribeEvent
  def onWorldSave(e: LevelEvent.Save): Unit = e.getLevel match {
    case level: ServerLevel =>
      for ((owner, pos) <- restoredTickets) {
        try {
          OpenComputers.log.warn(s"A chunk loader ticket has been orphaned! Address: ${owner}, position: (${pos.x}, ${pos.z}). Removing...")
          releaseTicket(level, owner.toString, pos)
        } catch {
          case err: Throwable => OpenComputers.log.error(err)
        }
      }
      restoredTickets.clear()
    case _ =>
  }

  @SubscribeEvent
  def onMove(e: RobotMoveEvent.Post): Unit = {
    val machineNode = e.agent.machine.node
    machineNode.reachableNodes.foreach(_.host match {
      case loader: UpgradeChunkloader => updateLoadedChunk(loader)
      case _ =>
    })
  }

  def releaseTicket(level: ServerLevel, addr: String, pos: ChunkPos): Unit = parseAddress(addr) match {
    case Some(uuid) =>
      for (x <- -1 to 1; z <- -1 to 1) {
        ticketController.forceChunk(level, uuid, pos.x + x, pos.z + z, false, true)
      }
    case _ => OpenComputers.log.warn(s"Address '$addr' could not be parsed")
  }

  def updateLoadedChunk(loader: UpgradeChunkloader): Unit = {
    (loader.host.getEnvironmentLevel, parseAddress(loader.node.address)) match {
      case (level: ServerLevel, Some(owner)) if loader.ticket.isDefined =>
        val blockPos = BlockPosition(loader.host)
        val centerChunk = new ChunkPos(blockPos.x >> 4, blockPos.z >> 4)
        if (centerChunk != loader.ticket.get) {
          val robotChunks = (for (x <- -1 to 1; z <- -1 to 1) yield new ChunkPos(centerChunk.x + x, centerChunk.z + z)).toSet
          val existingChunks = loader.ticket match {
            case Some(currPos) => (for (x <- -1 to 1; z <- -1 to 1) yield new ChunkPos(currPos.x + x, currPos.z + z)).toSet
            case None => immutable.Set.empty[ChunkPos]
          }
          for (toRemove <- existingChunks if !robotChunks.contains(toRemove)) {
            ticketController.forceChunk(level, owner, toRemove.x, toRemove.z, false, true)
          }
          for (toAdd <- robotChunks if !existingChunks.contains(toAdd)) {
            ticketController.forceChunk(level, owner, toAdd.x, toAdd.z, true, true)
          }
          loader.ticket = Some(centerChunk)
        }
      case _ =>
    }
  }
}