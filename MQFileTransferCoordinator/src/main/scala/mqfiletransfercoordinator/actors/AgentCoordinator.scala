package mqfiletransfercoordinator.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.AddProducer
import mqfiletransfercoordinator.messages.CommandMessage
import mqfiletransfercoordinator.messages.RemoveProducer
import mqfiletransfercoordinator.messages.ShutdownRequest
import mqfiletransfercoordinator.messages.ShutdownAgent
import mqfiletransfercoordinator.messages.AgentJoin
import mqfiletransfercoordinator.messages.AgentRemove
import mqfiletransfercoordinator.messages.TransferFailure
import scala.collection.mutable.Map

case class AgentRecord(server: String, commandQueueName: String, dataQueueName: String)

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
    }
    case a: Any => println(s"Unknown message type of ${a.getClass}")
  }
}

object AgentCoordinator {
  val agentMap = Map[String, AgentRecord]()
}