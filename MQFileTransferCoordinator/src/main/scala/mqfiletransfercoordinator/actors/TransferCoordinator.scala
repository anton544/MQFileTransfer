package mqfiletransfercoordinator.actors

import java.util.UUID
import scala.collection.mutable.Map
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.CancelTransfer
import mqfiletransfercoordinator.messages.CommandMessage
import mqfiletransfercoordinator.messages.InitiateTransfer
import mqfiletransfercoordinator.messages.TransferFailure
import mqfiletransfercoordinator.messages.TransferProgress
import mqfiletransfercoordinator.messages.TransferQuery
import mqfiletransfercoordinator.messages.TransferSuccess
import mqfiletransfercoordinator.messages.CancelTransferRequest
import mqfiletransfercoordinator.messages.TransferQueryAck
import mqfiletransfercoordinator.messages.TransferInitiated
import mqfiletransfercoordinator.messages.TransferSuccessfulReply
import mqfiletransfercoordinator.messages.TransferFailedReply
import mqfiletransfercoordinator.messages.InitiateAgentTransfer

case class TransferRecord(transferId: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String, var status: String, requestorQueueName: String)

class TransferCoordinator(commandQueueProducer: ActorRef, agentCoordinator: ActorRef) extends Actor with ActorLogging {
  import TransferCoordinator._

  def receive = LoggingReceive {
    case message: InitiateTransfer => {
      val guid = UUID.randomUUID().toString()
      transferMap += (guid -> TransferRecord(guid, message.sourceServer, message.sourcePath, message.targetServer, message.targetPath, "TransferInitiated", message.requestorQueueName))
      agentCoordinator ! InitiateAgentTransfer(guid, message.sourceServer, message.sourcePath, message.targetServer, message.targetPath)
      commandQueueProducer ! TransferInitiated(guid, message.requestorQueueName, message.correlationId)
    }
    case message: CancelTransferRequest => {
      val record = transferMap.get(message.transferId)
      record.map({ record =>
        record.status = "Cancelled"
        agentCoordinator ! CancelTransfer(record.transferId, record.sourceServer)
        agentCoordinator ! CancelTransfer(record.transferId, record.targetServer)
      })
    }
    case message: TransferProgress => transferMap.get(message.transferId).map(_.status = s"In Progess(${message.segmentNumber}/${message.segmentsTotal})")
    case message: TransferSuccess => transferMap.get(message.transferId) map { record =>
 	  record.status = "Success"
	  commandQueueProducer ! TransferSuccessfulReply(record.requestorQueueName, message.transferId)
    }
	case message: TransferFailure => transferMap.get(message.transferId) map { record =>
	  record.status = "Failed"
	  commandQueueProducer ! TransferFailedReply(record.requestorQueueName, message.transferId)  
	}
    case message: TransferQuery => transferMap.get(message.transferId).map((record: TransferRecord) => commandQueueProducer ! TransferQueryAck(message.queryReplyQueueName, message.transferId, record.status)) 
    case _ => log.warning("Unknown message type")
  }
}

object TransferCoordinator {
  val transferMap = Map[String, TransferRecord]()
}