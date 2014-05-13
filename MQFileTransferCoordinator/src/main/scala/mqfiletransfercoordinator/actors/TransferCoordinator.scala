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

case class TransferRecord(transferid: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String, var status: String)

class TransferCoordinator(commandQueueProducer: ActorRef, agentCoordinator: ActorRef) extends Actor with ActorLogging {
  import TransferCoordinator._

  def receive = LoggingReceive {
    case message: InitiateTransfer => {
      val guid = UUID.randomUUID().toString()
      transferMap += (guid -> TransferRecord(guid, message.sourceServer, message.sourcePath, message.targetServer, message.targetPath, "TransferInitiated"))
      agentCoordinator ! message
      commandQueueProducer ! TransferInitiated(guid, message.requestorQueueName, message.correlationId)
    }
    case message: CancelTransferRequest => {
      val record = transferMap.get(message.transferid)
      println(s"Found record for CancelTransferRequest: ${record.isDefined}")
      record.map({ (record: TransferRecord) =>
        record.status = "Cancelled"
        agentCoordinator ! CancelTransfer(record.transferid, record.sourceServer)
        agentCoordinator ! CancelTransfer(record.transferid, record.targetServer)
      })
    }
    case message: TransferProgress => transferMap.get(message.transferid).map(_.status = s"In Progess(${message.segmentNumber}/${message.segmentsTotal})")
    case message: TransferSuccess => transferMap.get(message.transferid).map(_.status = "Success")
    case message: TransferFailure => transferMap.get(message.transferid).map(_.status = "Failed")
    case message: TransferQuery => transferMap.get(message.transferid).map((record: TransferRecord) => commandQueueProducer ! TransferQueryAck(message.transferid, record.status)) 
    case _ => log.warning("Unknown message type")
  }
}

object TransferCoordinator {
  val transferMap = Map[String, TransferRecord]()
}