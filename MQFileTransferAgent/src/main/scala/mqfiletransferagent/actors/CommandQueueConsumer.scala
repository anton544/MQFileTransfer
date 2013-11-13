package mqfiletransferagent.actors

import akka.camel.Consumer
import akka.camel.CamelMessage
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import scala.xml.XML
import mqfiletransferagent.messages._

//Job: extract xml from CamelBody
class CommandQueueConsumer(commandQueue: String) extends Consumer with ActorLogging {
	def endpointUri = commandQueue
	
	def receive = LoggingReceive {
	  case camelMessage: CamelMessage => CommandMessage(camelMessage.bodyAs[String]) match {
	    case init: InitiateTransferCommandMessage => {}
	    case start: StartingTransferCommandMessage => {}
	    case startAck: StartingTransferAckCommandMessage => {}
	  }
	  case _ => log.warning("CommandQueueConsumer received unknown message type")
	}
}