package mqfiletransferagent.actors

import akka.camel.Consumer
import akka.camel.CamelMessage
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import scala.xml.XML
import mqfiletransferagent.messages._
import akka.actor.ActorRef

//Job: extract xml from CamelBody
class CommandQueueConsumer(commandQueue: String, agentCoordinator: ActorRef) extends Consumer with ActorLogging {
	def endpointUri = commandQueue
	
	def this(commandQueue: String) = this(commandQueue, null)
	
	def receive = LoggingReceive {
		case camelMessage: CamelMessage => {
			agentCoordinator ! CommandMessage(camelMessage.bodyAs[String])
		}
	  	case x: Any => {
	  		log.warning("CommandQueueConsumer received unknown message type: " + x)
	  	}
	}
}