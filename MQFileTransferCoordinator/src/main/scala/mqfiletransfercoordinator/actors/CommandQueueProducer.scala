package mqfiletransfercoordinator.actors

import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.Actor
import akka.camel.CamelExtension
import mqfiletransfercoordinator.messages.CommandMessage
import scala.collection.mutable.Map
import mqfiletransfercoordinator.messages.AddProducer
import mqfiletransfercoordinator.messages.RemoveProducer
import mqfiletransfercoordinator.messages.CancelTransfer

class CommandQueueProducer extends Actor with ActorLogging {
	import CommandQueueProducer._
	val producerTemplate = CamelExtension(context.system).template

	def receive = LoggingReceive {
		case message: CommandMessage => {
			queueNameMap.get(message.sourceServer).map((queueName: String) => producerTemplate.sendBody("activemq:queue:" + queueName, message.toXmlString()))
		}
		case addProducer: AddProducer => {
			queueNameMap += (addProducer.server -> addProducer.queueName)
		}
		case removeProducer: RemoveProducer => {
			queueNameMap -= removeProducer.server
		}
		case cancelTransfer: CancelTransfer => {
			queueNameMap.get(cancelTransfer.server).map((queueName: String) => producerTemplate.sendBody("activemq:queue:" + queueName, cancelTransfer.toXmlString))
		}
		case _ => log.warning("Unknown message type")
	}
}

object CommandQueueProducer {
	val queueNameMap = Map[String, String]()
} 