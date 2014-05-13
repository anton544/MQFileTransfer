package mqfiletransfercoordinator.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.camel.CamelMessage
import akka.camel.Consumer
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.AgentJoin
import mqfiletransfercoordinator.messages.AgentRemove
import mqfiletransfercoordinator.messages.CommandMessage
import mqfiletransfercoordinator.messages.InitiateTransfer
import mqfiletransfercoordinator.messages.CancelTransfer
import mqfiletransfercoordinator.messages.TransferProgress
import mqfiletransfercoordinator.messages.CancelTransferRequest
import mqfiletransfercoordinator.messages.TransferQuery
import mqfiletransfercoordinator.messages.TransferSuccess
import mqfiletransfercoordinator.messages.TransferFailure
import mqfiletransfercoordinator.messages.ShutdownRequest

class CommandQueueConsumer(commandQueueName: String, agentCoordinator: ActorRef, transferCoordinator: ActorRef) extends Actor with Consumer with ActorLogging {
  def endpointUri = "activemq:queue:" + commandQueueName

  def receive = LoggingReceive {
    case camelMessage: CamelMessage => {
      self ! CommandMessage(camelMessage.bodyAs[String])
    }
    case commandMessage: CommandMessage => {
      commandMessage.command match {
        case "Shutdown" => agentCoordinator ! ShutdownRequest
        case "AgentJoin" => agentCoordinator ! new AgentJoin(commandMessage.server, commandMessage.commandQueueName, commandMessage.dataQueueName)
        case "AgentRemove" => agentCoordinator ! new AgentRemove(commandMessage.server)
        case "InitiateTransfer" => transferCoordinator ! new InitiateTransfer(commandMessage.requestorQueueName, commandMessage.correlationId, commandMessage.sourceServer, commandMessage.sourcePath, commandMessage.targetServer, commandMessage.targetPath)
        case "CancelTransfer" => transferCoordinator ! new CancelTransferRequest(commandMessage.transferid)
        case "TransferStatus" => transferCoordinator ! new TransferProgress(commandMessage.transferid, commandMessage.segmentNumber, commandMessage.segmentsTotal) 
        case "TransferSuccess" => transferCoordinator ! new TransferSuccess(commandMessage.transferid)
        case "TransferFailure" => transferCoordinator ! new TransferFailure(commandMessage.transferid)
        case "TransferQuery" => transferCoordinator ! new TransferQuery(commandMessage.transferid, commandMessage.queryReplyQueueName)
      }
    }
    case _ => println("not implemented")
  }

}