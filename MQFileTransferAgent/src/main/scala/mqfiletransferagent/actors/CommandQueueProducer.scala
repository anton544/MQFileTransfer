package mqfiletransferagent.actors

import akka.actor.Actor
import akka.camel.CamelExtension
import scala.collection.mutable.Map
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.RemoveProducer
import mqfiletransferagent.messages.CommandMessage

class CommandQueueProducer extends Actor {
	import CommandQueueProducer._
	val camel = CamelExtension(context.system)
	val producerTemplate = camel.template
	
	def receive = {
		case addProducer: AddProducer => {
			mailboxMap += (addProducer.transferid -> addProducer.queuename)
		}
		case removeProducer: RemoveProducer => {
			mailboxMap -= (removeProducer.transferid)
		}
		case commandMessage: CommandMessage => {
			mailboxMap.get(commandMessage.transferid).map((mailbox: String) => producerTemplate.sendBody("activemq:queue:"+mailbox, commandMessage.toString()))
		}
	}
}

object CommandQueueProducer {
	val mailboxMap = Map[String, String]()
}