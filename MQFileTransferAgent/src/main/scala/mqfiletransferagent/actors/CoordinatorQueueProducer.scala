package mqfiletransferagent.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import mqfiletransferagent.messages.TransferProgress
import akka.camel.CamelExtension

class CoordinatorQueueProducer(coordinatorQueueName: String) extends Actor with ActorLogging {
	val camel = CamelExtension(context.system)
	val producerTemplate = camel.template
	
	def receive = {
		case progess: TransferProgress => {
			  producerTemplate.sendBody("activemq:queue:" + coordinatorQueueName, progess.toXmlString())
		}
	}
}