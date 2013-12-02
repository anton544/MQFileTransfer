package mqfiletransferagent.actors

import akka.actor.Actor
import akka.camel.CamelExtension
import scala.collection.mutable.Map
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.RemoveProducer
import mqfiletransferagent.messages.CommandMessage
import akka.event.LoggingReceive
import akka.actor.ActorLogging

class CommandQueueProducer extends Actor with ActorLogging {
	import CommandQueueProducer._
	val camel = CamelExtension(context.system)
	val producerTemplate = camel.template
	
	def receive = LoggingReceive {
		case addProducer: AddProducer => {
			log.debug("Adding Producer for %s using queue %s" format (addProducer.transferid, addProducer.queuename))
			mailboxMap += (addProducer.transferid -> addProducer.queuename)
			log.debug("mailboxMap after adding Producer: %s" format mailboxMap)
		}
		case removeProducer: RemoveProducer => {
			log.debug("Removing Producer for %s" format removeProducer.transferid)
			mailboxMap -= (removeProducer.transferid)
			log.debug("mailboxMap after removing Producer: %s" format mailboxMap)
		}
		case commandMessage: CommandMessage => {
			log.debug("Sending command message: %s" format commandMessage.toXmlString)
			mailboxMap.get(commandMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("activemq:queue:"+mailbox, commandMessage.toXmlString()))
		}
	}
}

object CommandQueueProducer {
	val mailboxMap = Map[String, String]()
}