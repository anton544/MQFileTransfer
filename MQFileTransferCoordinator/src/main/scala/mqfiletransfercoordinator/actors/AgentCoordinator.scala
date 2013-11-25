package mqfiletransfercoordinator.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import mqfiletransfercoordinator.messages.CommandMessage
import scala.collection.mutable.Map
import akka.actor.ActorRef
import mqfiletransfercoordinator.messages.AddProducer
import mqfiletransfercoordinator.messages.RemoveProducer


class AgentCoordinator(commandProducer: ActorRef) extends Actor with ActorLogging {
    import AgentCoordinator._
    
	def receive = LoggingReceive {
		case commandMessage: CommandMessage => processCommand(commandMessage)
	}
	
	def processCommand(message: CommandMessage) {
		message.command match {
			case "AgentJoin" => {
				commandProducer ! AddProducer(message.server, message.commandQueueName)
				
			}
			case "AgentRemove" => {
				commandProducer ! RemoveProducer(message.server)
			}
		}
	}
}

object AgentCoordinator {
}