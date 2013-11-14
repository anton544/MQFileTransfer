package mqfiletransferagent.actors

import akka.camel.Consumer
import akka.camel.CamelMessage
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import scala.xml.XML
import mqfiletransferagent.messages._

class DataQueueConsumer(dataQueue: String) extends Consumer with ActorLogging {
	def endpointUri = dataQueue
	
	def receive = LoggingReceive {
	  case message: CamelMessage => DataTransferMessage(message.bodyAs[String]).command match {
	    case "CompletingTransfer" => {}
	    case "CompletingTransferAck" => {}
	    case "DataTransferAck" => {}
	    case "DataTransfer" => {}
	  }
	}
}