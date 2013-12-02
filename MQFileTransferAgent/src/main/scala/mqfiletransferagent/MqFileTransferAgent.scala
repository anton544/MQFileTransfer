package mqfiletransferagent

import akka.actor.ActorSystem
import akka.camel.CamelExtension
import akka.actor.Props
import mqfiletransferagent.actors.CommandQueueConsumer
import mqfiletransferagent.actors.AgentTransferCoordinator
import mqfiletransferagent.actors.DataQueueConsumer
import mqfiletransferagent.actors.CoordinatorQueueProducer
import mqfiletransferagent.actors.CommandQueueProducer
import mqfiletransferagent.actors.DataQueueProducer
import mqfiletransferagent.actors.FileActor
import org.apache.activemq.camel.component.ActiveMQComponent

object MqFileTransferAgent extends App {
	val usage = """
	  Usage: MqFileTransferAgent [--commandq commandQueueName] [--dataq dataQueueName] activeMqConnectionString
	"""
	  
	if (args.length == 0) {
	  println(usage)
	  sys.exit(-1)
	}
	
	def nextOption(map: Map[Symbol, Any], list: List[String]): Map[Symbol, Any] = {
	  list match {
	    case Nil => map
	    case "--commandq" :: commandQueueName :: tail =>
	      nextOption(map ++ Map('commandq -> commandQueueName), tail)
	    case "--dataq" :: dataQueueName :: tail =>
	      nextOption(map ++ Map('dataq -> dataQueueName), tail)
	    case "--coordinatorq" :: coordinatorQueueName :: tail =>
	      nextOption(map ++ Map('coordinatorq -> coordinatorQueueName), tail)
	    case "--transferSize" :: transferSize :: tail =>
	      nextOption(map ++ Map('transferSize -> transferSize.toInt), tail)
	    case activeMqConnectionString :: Nil =>
	      nextOption(map ++ Map('activeMqConnectionString -> activeMqConnectionString), list.tail)
	    case option :: tail =>
	      println("Unknown option " + option)
	      sys.exit(-1)
	  }
	}
	  
	val options = nextOption(Map(), args.toList)
	val system = ActorSystem("MqFileTransferAgent")
	val camelExtension = CamelExtension(system)
	val camelContext = camelExtension.context
	camelContext.addComponent("activemq", ActiveMQComponent.activeMQComponent(options.get('activeMqConnectionString).get.asInstanceOf[String]))
	val coordinatorQueueProducer = system.actorOf(Props(new CoordinatorQueueProducer(options.getOrElse('coordinatorq, "COORDINATOR.COMMAND.QUEUE").asInstanceOf[String])), "coordinatorQueueProducer")
	val commandQueueProducer = system.actorOf(Props[CommandQueueProducer], "commandQueueProducer")
	val dataQueueProducer = system.actorOf(Props[DataQueueProducer], "dataQueueProducer")
	val fileActor = system.actorOf(Props(new FileActor(dataQueueProducer, None, coordinatorQueueProducer, options.getOrElse('transferSize, 1024 * 512).asInstanceOf[Int])), "fileActor")
	val agentTransferCoordinator = system.actorOf(Props(new AgentTransferCoordinator(dataQueueProducer, commandQueueProducer, fileActor, coordinatorQueueProducer)), "agentTransferCoordinator")
	val commandQueueConsumer = system.actorOf(Props(new CommandQueueConsumer(options.getOrElse('commandq, "activemq:queue:AGENT.COMMAND.QUEUE").asInstanceOf[String], agentTransferCoordinator)), "commandQueueConsumer")
	val dataQueueConsumer = system.actorOf(Props(new DataQueueConsumer(options.getOrElse('dataq, "activemq:queue:AGENT.DATA.QUEUE").asInstanceOf[String], agentTransferCoordinator)), "dataQueueConsumer")
	
	def shutdown() {
		system.shutdown
		system.awaitTermination
	}
}