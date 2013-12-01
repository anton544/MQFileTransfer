package mqfiletransferagent.actors

import akka.camel.Consumer
import akka.camel.CamelMessage
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import scala.xml.XML
import mqfiletransferagent.messages._
import akka.actor.ActorRef

class DataQueueConsumer(dataQueue: String, agentCoordinator: ActorRef) extends Consumer with ActorLogging {
	def endpointUri = dataQueue
	
	def this(dataQueue: String) = this(dataQueue, null)
	
	def receive = LoggingReceive {
	  case message: CamelMessage => agentCoordinator ! DataTransferMessage(message.bodyAs[String])
	  case x: Any => {
	  		log.warning("CommandQueueConsumer received unknown message type: " + x.getClass)
	  	}
	}
}