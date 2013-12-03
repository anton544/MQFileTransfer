package mqfiletransferagent.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import mqfiletransferagent.messages.TransferProgress
import akka.camel.CamelExtension
import akka.event.LoggingReceive
import mqfiletransferagent.messages.CommandMessage

class CoordinatorQueueProducer(coordinatorQueueName: String) extends Actor with ActorLogging {
	val camel = CamelExtension(context.system)
	val producerTemplate = camel.template
	
	def receive = LoggingReceive {
		case progess: TransferProgress => {
			producerTemplate.sendBody(coordinatorQueueName, progess.toXmlString())
		}
		case commandMessage: CommandMessage => {
			producerTemplate.sendBody(coordinatorQueueName, commandMessage.toXmlString())
		}
	}
}