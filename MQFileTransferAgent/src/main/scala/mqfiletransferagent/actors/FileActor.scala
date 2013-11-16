package mqfiletransferagent.actors

import akka.actor.Actor
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import org.apache.commons.codec.binary.Base64
import mqfiletransferagent.messages.DataTransferMessage
import mqfiletransferagent.messages.FileData
import scala.xml.Elem
import akka.event.LoggingReceive
import akka.actor.ActorLogging
import akka.actor.ActorRef

class FileActor(dataProducer: ActorRef, transferCoordiator: ActorRef, coordinatorProducer: ActorRef) extends Actor with ActorLogging {
	def this() = this(null, null, null)
	def receive = LoggingReceive {
		case fileData: FileData => {
			val stream = new FileOutputStream(fileData.filename, true)
			IOUtils.write(Base64.decodeBase64(fileData.data), stream)
			stream.close()
			dataProducer ! new DataTransferMessage(fileData)
		}
		case _ => log.warning("Unknown message type received")
	}
	
	implicit def toDataTransferAck(fileData: FileData): Elem = <message><type>DataTransferAck</type><transferid>{fileData.transferid}</transferid><segmentnumber>{fileData.segmentNumber}</segmentnumber><status>Success</status></message>
}