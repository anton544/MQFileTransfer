package mqfiletransfercoordinator.actors

import akka.actor.Actor
import akka.camel.Consumer
import akka.actor.ActorLogging
import akka.event.LoggingReceive

class CommandQueueConsumer(commandQueueName: String) extends Actor with Consumer with ActorLogging {
	def endpointUri = "activemq:queue:" + commandQueueName
	
	def receive = LoggingReceive {
	  case _ => println("not implemented")
	}

}