package mqfiletransferagent.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.camel.CamelExtension
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.RemoveProducer
import scala.collection.mutable.Map
import mqfiletransferagent.messages.DataTransferMessage

class DataQueueProducer extends Actor with ActorLogging {
	import DataQueueProducer._
	val camel = CamelExtension(context.system)
	val producerTemplate = camel.template
	def receive = {
		case addProducer: AddProducer => {
			mailboxMap += (addProducer.transferid -> addProducer.queuename)
		}
		case removeProducer: RemoveProducer => {
			mailboxMap -= (removeProducer.transferid)
		}
		case dataTransferMessage: DataTransferMessage => {
			mailboxMap.get(dataTransferMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("activemq:queue:"+mailbox, dataTransferMessage.toXmlString()))
		}
	}
}

object DataQueueProducer {
	val mailboxMap = Map[String, String]()
}