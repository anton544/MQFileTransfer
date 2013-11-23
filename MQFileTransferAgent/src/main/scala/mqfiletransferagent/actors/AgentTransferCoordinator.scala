package mqfiletransferagent.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import mqfiletransferagent.messages.CommandMessage
import mqfiletransferagent.messages.DataTransferMessage
import scala.collection.mutable.HashMap
import akka.actor.ActorRef
import mqfiletransferagent.messages.RemoveProducer
import mqfiletransferagent.messages.CleanupFile
import mqfiletransferagent.messages.FileWriteVerify
import mqfiletransferagent.messages.FileWriteSuccess
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.FileVerify
import mqfiletransferagent.messages.FileData
import mqfiletransferagent.messages.FileWriteFailure
import java.io.File
import java.io.FileInputStream
import org.apache.commons.codec.digest.DigestUtils

class AgentTransferCoordinator(dataProducer: ActorRef, cmdProducer: ActorRef, fileActor: ActorRef) extends Actor with ActorLogging {
	def this() = this(null, null, null)

	import AgentTransferCoordinator._
	def receive = {
		case command: CommandMessage => processCommand(command)
		case data: DataTransferMessage => processData(data)
		case writeSuccess: FileWriteSuccess => {
			dataProducer ! AddProducer(writeSuccess.transferid, writeSuccess.path)
			cmdProducer ! AddProducer(writeSuccess.transferid, writeSuccess.path)
			cmdProducer ! new CommandMessage(<message><type>StartTransferAck</type><transferid>{writeSuccess.transferid}</transferid><status>Success</status></message>)
			pathMap += (writeSuccess.transferid -> writeSuccess.path)
		}
		case writeFailure: FileWriteFailure => {
			cmdProducer ! new CommandMessage(<message><type>StartTransferAck</type><transferid>{writeFailure.transferid}</transferid><status>Fail</status></message>)
			cmdProducer ! RemoveProducer(writeFailure.transferid)
		}
		
		case x:Any => log.warning("Unknown message[" + x.getClass + "]: " + x.toString)
	}
	
	def processCommand(commandMessage: CommandMessage) {
		commandMessage.command match {
			case "CancelTransfer" => {
				dataProducer ! RemoveProducer(commandMessage.transferid)
				cmdProducer ! RemoveProducer(commandMessage.transferid)
				pathMap.get(commandMessage.transferid).map(fileActor ! CleanupFile(_))
				AgentTransferCoordinator.pathMap -= commandMessage.transferid
			}
			case "StartTransfer" => {
				fileActor ! FileWriteVerify(commandMessage.transferid, commandMessage.targetPath)
			}
		}
	}
	
	def processData(dataMessage: DataTransferMessage) {
		dataMessage.command match {
			case "DataTransfer" => {
				pathMap.get(dataMessage.transferid).map(fileActor ! FileData(dataMessage.data, _, dataMessage.transferid, dataMessage.segmentNumber, dataMessage.segmentsTotal))
			}
			case "DataTransferComplete" => {
				pathMap.get(dataMessage.transferid).map(fileActor ! FileVerify(dataMessage.transferid, _, dataMessage.md5hash))
			}
		}
	}
}

object AgentTransferCoordinator {
	val pathMap = HashMap[String, String]()
}