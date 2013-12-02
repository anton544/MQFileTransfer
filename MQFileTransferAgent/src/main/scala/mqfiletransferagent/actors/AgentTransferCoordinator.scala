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
import mqfiletransferagent.messages.TransferNextSegment
import mqfiletransferagent.messages.FileReadFailure
import mqfiletransferagent.messages.FileReadVerify
import mqfiletransferagent.messages.FileReadSuccess
import akka.event.LoggingReceive

class AgentTransferCoordinator(dataProducer: ActorRef, cmdProducer: ActorRef, fileActor: ActorRef, coordinatorProducer: ActorRef) extends Actor with ActorLogging {

	import AgentTransferCoordinator._
	def receive = LoggingReceive {
		case command: CommandMessage => processCommand(command)
		case data: DataTransferMessage => processData(data)
		case writeSuccess: FileWriteSuccess => {
			dataProducer ! AddProducer(writeSuccess.transferid, writeSuccess.sourceDataQueue)
			cmdProducer ! new CommandMessage(<message><type>StartTransferAck</type><transferid>{writeSuccess.transferid}</transferid><status>Success</status></message>)
			pathMap += (writeSuccess.transferid -> writeSuccess.path)
		}
		case readSuccess: FileReadSuccess => {
			cmdProducer ! AddProducer(readSuccess.transferid, readSuccess.targetCommandQueue)
			dataProducer ! AddProducer(readSuccess.transferid, readSuccess.targetDataQueue)
			cmdProducer ! new CommandMessage(<message><type>StartTransfer</type><transferid>{readSuccess.transferid}</transferid><targetpath>{readSuccess.targetPath}</targetpath><targetcommandqueue>{readSuccess.targetCommandQueue}</targetcommandqueue><targetdataqueue>{readSuccess.targetDataQueue}</targetdataqueue></message>)
		}
		case writeFailure: FileWriteFailure => {
			cmdProducer ! new CommandMessage(<message><type>StartTransferAck</type><transferid>{writeFailure.transferid}</transferid><status>Fail</status></message>)
			cmdProducer ! RemoveProducer(writeFailure.transferid)
		}
		case readFailure: FileReadFailure => {
			coordinatorProducer ! new CommandMessage(<message><type>TransferFailure</type><transferid>1234</transferid></message>)
		}
		
		case x:Any => log.warning("Unknown message[" + x.getClass + "]: " + x.toString)
	}
	
	def processCommand(commandMessage: CommandMessage) {
		log.debug("processing Command: %s" format commandMessage.command)
		commandMessage.command match {
			case "CancelTransfer" => {
				log.debug("Canceling transfer=: %s" format commandMessage.transferid)
				dataProducer ! RemoveProducer(commandMessage.transferid)
				cmdProducer ! RemoveProducer(commandMessage.transferid)
				pathMap.get(commandMessage.transferid).map(fileActor ! CleanupFile(_))
				pathMap -= commandMessage.transferid
				log.debug("Pathmap after removal:" + pathMap)
			}
			case "StartTransfer" => {
				log.debug("Starting transfer with id: %s, file: %s" format(commandMessage.transferid, commandMessage.targetPath))
				cmdProducer ! AddProducer(commandMessage.transferid, commandMessage.sourceCommandQueue)
				fileActor ! FileWriteVerify(commandMessage.transferid, commandMessage.targetPath, commandMessage.sourceDataQueue)
			}
			case "InitiateTransfer" => {
				fileActor ! FileReadVerify(commandMessage.transferid, commandMessage.sourcePath, commandMessage.targetPath, commandMessage.targetCommandQueue, commandMessage.targetDataQueue)
			}
		}
	}
	
	def processData(dataMessage: DataTransferMessage) {
		dataMessage.command match {
			case "DataTransfer" => {
				pathMap.get(dataMessage.transferid).map(fileActor ! FileData(dataMessage.data, _, dataMessage.transferid, dataMessage.segmentNumber, dataMessage.segmentsTotal))
			}
			case "DataTransferAck" => {
				pathMap.get(dataMessage.transferid).map(fileActor ! TransferNextSegment(dataMessage.transferid, _, dataMessage.segmentNumber + 1))
			}
			case "DataTransferComplete" => {
				pathMap.get(dataMessage.transferid).map(fileActor ! FileVerify(dataMessage.transferid, _, dataMessage.md5hash))
			}
			case "DataTransferCompleteAck" => {
				cmdProducer ! RemoveProducer(dataMessage.transferid)
				dataProducer ! RemoveProducer(dataMessage.transferid)
				if (dataMessage.status == "Success")
					coordinatorProducer ! new CommandMessage(<message><type>TransferSuccess</type><transferid>{dataMessage.transferid}</transferid></message>)
				else
					coordinatorProducer ! new CommandMessage(<message><type>TransferFailure</type><transferid>{dataMessage.transferid}</transferid></message>)
			}
		}
	}
}

object AgentTransferCoordinator {
	val pathMap = HashMap[String, String]()
}