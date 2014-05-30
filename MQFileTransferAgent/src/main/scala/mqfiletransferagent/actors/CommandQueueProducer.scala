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
			log.debug(s"Adding Producer for ${addProducer.transferid} using queue ${addProducer.queuename}")
			mailboxMap += (addProducer.transferid -> addProducer.queuename)
			log.debug(s"mailboxMap after adding Producer: ${mailboxMap}")
		}
		case removeProducer: RemoveProducer => {
			log.debug(s"Removing Producer for ${removeProducer.transferid}")
			mailboxMap -= (removeProducer.transferid)
			log.debug(s"mailboxMap after removing Producer: ${mailboxMap}")
		}
		case commandMessage: CommandMessage => {
			log.debug(s"Sending command message: ${commandMessage.toXmlString}")
			mailboxMap.get(commandMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("mq:queue:"+mailbox, commandMessage.toXmlString()))
		}
	}
}

object CommandQueueProducer {
	val mailboxMap = Map[String, String]()
}