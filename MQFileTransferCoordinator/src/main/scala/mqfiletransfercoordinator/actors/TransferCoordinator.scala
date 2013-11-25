package mqfiletransfercoordinator.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.CommandMessage
import akka.actor.ActorRef
import java.util.UUID
import scala.collection.mutable.Map
import mqfiletransfercoordinator.messages.CancelTransfer

case class TransferRecord(transferid: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String, var status: String)

class TransferCoordinator(commandQueueProducer: ActorRef) extends Actor with ActorLogging {
	import TransferCoordinator._
	
	def receive = LoggingReceive {
		case commandMessage: CommandMessage => processCommand(commandMessage, sender)
		case _ => log.warning("Unknown message type")
	}
	
	def processCommand(message: CommandMessage, sender: ActorRef) {
		message.command match {
			case "CancelTransfer" => {
				transferMap.get(message.transferid).map({ (record: TransferRecord) =>
				  	record.status = "Cancelled"
					commandQueueProducer ! CancelTransfer(record.transferid, record.sourceServer)
					commandQueueProducer ! CancelTransfer(record.transferid, record.targetServer)
				})
			}
			case "InitiateTransfer" => {
				val guid = UUID.randomUUID().toString()
				transferMap += (guid -> TransferRecord(guid, message.sourceServer, message.sourcePath, message.targetServer, message.targetPath, "TransferInitiated"))
				sender ! new CommandMessage(<message><type>TransferInitiated</type><transferid>{guid}</transferid></message>)
				commandQueueProducer ! new CommandMessage(<message><type>InitiateTransfer</type><transferid>{guid}</transferid><sourceserver>{message.sourceServer}</sourceserver><sourcepath>{message.sourcePath}</sourcepath><targetserver>{message.targetServer}</targetserver><targetpath>{message.targetPath}</targetpath></message>)
			}
			case "TransferSuccess" => {
				transferMap.get(message.transferid).map(_.status = "Success")
			}
			case "TransferFailure" => {
				transferMap.get(message.transferid).map(_.status = "Failed")
			}
			case "TransferQuery" => {
				transferMap.get(message.transferid).map((record: TransferRecord) => sender ! new CommandMessage(<message><type>TransferQueryAck</type><transferid>1234</transferid><status>{record.status}</status></message>))
			}
			case "TransferStatus" => {
				transferMap.get(message.transferid).map(_.status = "In Progess(%d/%d)" format (message.segmentNumber, message.segmentsTotal))
			}
		}
	}
}

object TransferCoordinator {
	val transferMap = Map[String, TransferRecord]()
}