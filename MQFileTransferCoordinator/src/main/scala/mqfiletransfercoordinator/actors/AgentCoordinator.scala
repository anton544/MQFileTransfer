package mqfiletransfercoordinator.actors

import scala.collection.mutable.Map
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.AgentJoin
import mqfiletransfercoordinator.messages.AgentRemove
import mqfiletransfercoordinator.messages.CancelTransferCommand
import mqfiletransfercoordinator.messages.InitiateTransfer
import mqfiletransfercoordinator.messages.ShutdownAgent
import mqfiletransfercoordinator.messages.ShutdownRequest
import mqfiletransfercoordinator.messages.CancelTransferAgentCommand
import mqfiletransfercoordinator.messages.InitiateAgentTransfer
import mqfiletransfercoordinator.messages.InitiateAgentTransferCommand

case class AgentRecord(server: String, commandQueueName: String, dataQueueName: String)
case class SHUTDOWN()

class AgentCoordinator(commandProducer: ActorRef) extends Actor with ActorLogging {
  import AgentCoordinator._

  def receive = LoggingReceive {
    case message: AgentJoin => {
      agentMap += (message.server -> AgentRecord(message.server, message.commandQueue, message.dataQueue))
    }
    case message: AgentRemove => {
      agentMap -= (message.server)
    }
    case message: ShutdownRequest => {
      for (row <- agentMap) { commandProducer ! ShutdownAgent(row._2.commandQueueName) }
      commandProducer ! SHUTDOWN
    }
    case message: InitiateAgentTransfer => {
      agentMap.get(message.sourceServer) map { source =>
        agentMap.get(message.targetServer) map { target =>
          commandProducer ! InitiateAgentTransferCommand(message.transferId, source.commandQueueName, source.dataQueueName, message.sourcePath,
            target.commandQueueName, target.dataQueueName, message.targetPath)
        }
      }
    }
    case message: CancelTransferCommand => {
      agentMap.get(message.sourceServer) map { record =>
        commandProducer ! CancelTransferAgentCommand(record.commandQueueName, message.transferId)
      }
      agentMap.get(message.targetServer) map { record =>
        commandProducer ! CancelTransferAgentCommand(record.commandQueueName, message.transferId)
      }
    }
    case a: Any => println(s"Unknown message type of ${a.getClass}")
  }
}

object AgentCoordinator {
  val agentMap = Map[String, AgentRecord]()
}