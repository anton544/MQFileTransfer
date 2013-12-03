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
import mqfiletransferagent.messages.TransferNextSegment
import mqfiletransferagent.MqFileTransferAgent
import java.io.InputStream
import org.apache.commons.io.FileUtils
import mqfiletransferagent.messages.FileReadVerify
import mqfiletransferagent.messages.FileReadSuccess
import mqfiletransferagent.messages.FileReadFailure
import mqfiletransferagent.messages.RemoveProducer

class FileActor(dataProducer: ActorRef, agentTransferCoordinator: Option[ActorRef], coordinatorProducer: ActorRef, segmentMaxSize: Int = 1024 * 512) extends Actor with ActorLogging {
	val transferCoordinator:ActorRef = agentTransferCoordinator.getOrElse(context.actorFor("/user/agentTransferCoordinator"))
	def receive = LoggingReceive {
		case fileData: FileData => {
			val stream = new FileOutputStream(fileData.filename, true)
			IOUtils.write(Base64.decodeBase64(fileData.data), stream)
			stream.close()
			dataProducer ! new DataTransferMessage(<message><type>DataTransferAck</type><transferid>{fileData.transferid}</transferid><segmentnumber>{fileData.segmentNumber}</segmentnumber><status>Success</status></message>)
			coordinatorProducer ! TransferProgress(fileData.transferid, fileData.segmentNumber, fileData.segmentsTotal)
		}
		case fileVerify: FileVerify => {
		    log.debug(fileVerify.toString())
			val status = if (fileVerify.md5hash == hashFile(fileVerify.path)) "Success" else "Failure"
			dataProducer ! new DataTransferMessage(<message><type>DataTransferCompleteAck</type><transferid>{fileVerify.transferid}</transferid><status>{status}</status></message>)
		    dataProducer ! RemoveProducer(fileVerify.transferid)
		}
		case fileWriteVerify: FileWriteVerify => {
			log.debug(fileWriteVerify.toString())
			val file = new File(fileWriteVerify.path)
			var canWrite = false
			if (file.exists()) {
				if (file.canWrite())
				    canWrite = true
			} else {
				try {
				val created = file.createNewFile()
				if (created)
				    canWrite = true
				} catch {
					case _: java.io.IOException => 
				}
				file.delete()
			}
		    if (canWrite)
		    	transferCoordinator ! FileWriteSuccess(fileWriteVerify.transferid, fileWriteVerify.path, fileWriteVerify.sourceDataQueue)
		    else
		    	transferCoordinator ! FileWriteFailure(fileWriteVerify.transferid)
		}
		case readVerify: FileReadVerify => {
			log.debug(readVerify.toString())
			val file = new File(readVerify.sourcePath)
			if (file.exists() && file.canRead())
				transferCoordinator ! FileReadSuccess(readVerify.transferid, readVerify.sourcePath, readVerify.targetPath, readVerify.sourceCommandQueue, readVerify.sourceDataQueue, readVerify.targetCommandQueue, readVerify.targetDataQueue)
			else
				transferCoordinator ! FileReadFailure(readVerify.transferid)
		}
		case next: TransferNextSegment => {
			log.debug(next.toString())
			val file = new File(next.path)
			val startByte = segmentMaxSize * (next.nextSegmentNumber - 1)
			if (startByte >= file.length()) {
				dataProducer ! new DataTransferMessage(<message><type>DataTransferComplete</type><transferid>{next.transferid}</transferid><md5hash>{hashFile(next.path)}</md5hash></message>)
			} else {
				val endByte = if (file.length() < startByte + segmentMaxSize) file.length() else startByte + segmentMaxSize
				val data = getSegment(file, startByte, (endByte - startByte).toInt)
				val segmentstotal = file.length() / segmentMaxSize + (if ( file.length % segmentMaxSize > 0) 1 else 0)
				dataProducer ! new DataTransferMessage(<message><type>DataTransfer</type><transferid>{next.transferid}</transferid><segmentnumber>{next.nextSegmentNumber}</segmentnumber><segmentstotal>{segmentstotal}</segmentstotal><data>{data}</data></message>)
			}
		}
		case cleanupFile: CleanupFile => {
			log.debug(cleanupFile.toString())
			new File(cleanupFile.path).delete()
		}
		case _ => log.warning("Unknown message type received")
	}
	
	def getSegment(file: File, startByte: Long, segmentSize: Int) = {
		val is = FileUtils.openInputStream(file)
		is.skip(startByte)
		val segmentBytes = new Array[Byte](segmentSize)
		var bytesRead = is.read(segmentBytes)
		while (bytesRead != segmentSize) {
			bytesRead += is.read(segmentBytes, bytesRead, segmentSize-bytesRead)
		}
		is.close()
		Base64.encodeBase64String(segmentBytes)
	}
	
	def hashFile(path: String) = {
		val fis = new FileInputStream(new File(path))
		val hash = DigestUtils.md5Hex(fis)
		fis.close()
		hash
	}
}