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
			log.debug("Adding Producer for %s using queue %s" format (addProducer.transferid, addProducer.queuename))
			mailboxMap += (addProducer.transferid -> addProducer.queuename)
			log.debug("mailboxMap after adding Producer: %s" format mailboxMap)
		}
		case removeProducer: RemoveProducer => {
			log.debug("Removing Producer for %s" format removeProducer.transferid)
			mailboxMap -= (removeProducer.transferid)
			log.debug("mailboxMap after removing Producer: %s" format mailboxMap)
		}
		case dataTransferMessage: DataTransferMessage => {
			log.debug("Sending DataTransfer message: %s" format dataTransferMessage.toXmlString)
			mailboxMap.get(dataTransferMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("activemq:queue:"+mailbox, dataTransferMessage.toXmlString()))
		}
	}
}

object DataQueueProducer {
	val mailboxMap = Map[String, String]()
}