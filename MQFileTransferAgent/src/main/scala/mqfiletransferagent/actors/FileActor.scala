package mqfiletransferagent.actors

import akka.actor.Actor
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import org.apache.commons.codec.binary.Base64
import mqfiletransferagent.messages.DataTransferMessage
import mqfiletransferagent.messages.FileData
import scala.xml.Elem
import akka.event.LoggingReceive
import akka.actor.{ActorLogging,ActorRef}
import mqfiletransferagent.messages.FileVerify
import java.io.FileInputStream
import java.io.File
import org.apache.commons.codec.digest.DigestUtils
import mqfiletransferagent.messages.TransferProgress
import mqfiletransferagent.messages.FileWriteVerify
import mqfiletransferagent.messages.FileWriteFailure
import mqfiletransferagent.messages.CleanupFile
import mqfiletransferagent.messages.FileWriteSuccess
import java.io.IOException

class FileActor(dataProducer: ActorRef, transferCoordinator: ActorRef, coordinatorProducer: ActorRef) extends Actor with ActorLogging {
	def this() = this(null, null, null)
	def receive = LoggingReceive {
		case fileData: FileData => {
			val stream = new FileOutputStream(fileData.filename, true)
			IOUtils.write(Base64.decodeBase64(fileData.data), stream)
			stream.close()
			dataProducer ! new DataTransferMessage(fileData)
			coordinatorProducer ! TransferProgress(fileData.transferid, fileData.segmentNumber, fileData.segmentsTotal)
		}
		case fileVerify: FileVerify => {
		    println("fileVerify")
			val fis = new FileInputStream(new File(fileVerify.path))
			val status = if (fileVerify.md5hash == DigestUtils.md5Hex(fis)) "Success" else "Failure"
			dataProducer ! new DataTransferMessage(<message><type>DataTransferCompleteAck</type><transferid>{fileVerify.transferid}</transferid><status>{status}</status></message>)
			fis.close()
		}
		case fileWriteVerify: FileWriteVerify => {
			val file = new File(fileWriteVerify.path)
			if (file.exists()) {
				if (file.canWrite())
				    transferCoordinator ! FileWriteSuccess(fileWriteVerify.transferid, fileWriteVerify.path)
				else
				    transferCoordinator ! FileWriteFailure(fileWriteVerify.transferid)
			} else {
				try {
				val created = file.createNewFile()
				if (created)
				    transferCoordinator ! FileWriteSuccess(fileWriteVerify.transferid, fileWriteVerify.path)
				else 
				    transferCoordinator ! FileWriteFailure(fileWriteVerify.transferid)
				} catch {
					case _: java.io.IOException => 
						transferCoordinator ! FileWriteFailure(fileWriteVerify.transferid)
				}
				file.delete()
			}
		    
		}
		case cleanupFile: CleanupFile => {
			new File(cleanupFile.path).delete()
		}
		case _ => log.warning("Unknown message type received")
	}
	
	implicit def toDataTransferAck(fileData: FileData): Elem = <message><type>DataTransferAck</type><transferid>{fileData.transferid}</transferid><segmentnumber>{fileData.segmentNumber}</segmentnumber><status>Success</status></message>
}