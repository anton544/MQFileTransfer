package mqfiletransferagent.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.camel.CamelExtension
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.RemoveProducer
import scala.collection.mutable.Map
import mqfiletransferagent.messages.DataTransferMessage
import akka.event.LoggingReceive

class DataQueueProducer extends Actor with ActorLogging {
	import DataQueueProducer._
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
		case dataTransferMessage: DataTransferMessage => {
			log.debug(s"Sending DataTransfer message: ${dataTransferMessage.toXmlString}")
			mailboxMap.get(dataTransferMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("activemq:queue:"+mailbox, dataTransferMessage.toXmlString()))
		}
	}
}

object DataQueueProducer {
	val mailboxMap = Map[String, String]()
}