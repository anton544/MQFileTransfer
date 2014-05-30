package mqfiletransfercoordinator

import org.apache.activemq.camel.component.ActiveMQComponent
import akka.actor.ActorSystem
import akka.camel.CamelExtension
import scala.annotation.tailrec
import akka.actor.Props
import mqfiletransfercoordinator.actors.CommandQueueProducer
import mqfiletransfercoordinator.actors.AgentCoordinator
import mqfiletransfercoordinator.actors.TransferCoordinator
import mqfiletransfercoordinator.actors.CommandQueueConsumer

object MqFileTransferCoordinator extends App {
	val usage = """
	  Usage: MqFileTransferCoordinator --commandq commandQueueName --mqSystem mqsystem [--websphereMqHostName HostName] [--websphereMqQueueManager QueueManager] [--websphereMqChannel Channel] [--websphereMqPort Port] [--activeMqConnectionString activeMqConnectionString]
	"""
	  
	if (args.length == 0) {
	  println(usage)
	  sys.exit(-1)
	}
	
	@tailrec
	private def nextOption(map: Map[Symbol, Any], list: List[String]): Map[Symbol, Any] = {
	  list match {
	    case Nil => map
	    case "--commandq" :: commandQueueName :: tail =>
	      nextOption(map ++ Map('commandq -> commandQueueName), tail)
	    case "--mqSystem" :: mqsystem :: tail =>
	      nextOption(map ++ Map('mqSystem -> mqsystem), tail)
	    case "--websphereMqHostName" :: websphereMqHostName :: tail =>
	      nextOption(map ++ Map('websphereMqHostName -> websphereMqHostName), tail)
	    case "--websphereMqQueueManager" :: websphereMqQueueManager :: tail =>
	      nextOption(map ++ Map('websphereMqQueueManager -> websphereMqQueueManager), tail)
	    case "--websphereMqChannel" :: websphereMqChannel :: tail =>
	      nextOption(map ++ Map('websphereMqChannel -> websphereMqChannel), tail)
	    case "--websphereMqPort" :: websphereMqPort :: tail =>
	      nextOption(map ++ Map('websphereMqPort -> websphereMqPort.toInt), tail)
	    case "--activeMqConnectionString" :: activeMqConnectionString :: Nil =>
	      nextOption(map ++ Map('activeMqConnectionString -> activeMqConnectionString), list.tail)
	    case option :: tail =>
	      println("Unknown option " + option)
	      sys.exit(-1)
	  }
	}
	
	def shutdown() {
		system.shutdown
		Thread.sleep(5000)
		System.exit(0)
	}
	  
	val options = nextOption(Map(), args.toList)
	val system = ActorSystem("MqFileTransferCoordinator")
	val camelExtension = CamelExtension(system)
	val camelContext = camelExtension.context
	camelContext.addComponent("mq", getJmsComponent(options))
	val commandProducer = system.actorOf(Props(new CommandQueueProducer(camelExtension)))
	val agentCoordinator = system.actorOf(Props(new AgentCoordinator(commandProducer)))
	val transferCoordinator = system.actorOf(Props(new TransferCoordinator(commandProducer, agentCoordinator)))
	val commandQueueConsumer = system.actorOf(Props(new CommandQueueConsumer("mq:queue:" + options.get('commandq).getOrElse("COORDINATOR.COMMAND.QUEUE").asInstanceOf[String], agentCoordinator, transferCoordinator)))
	
	private def getJmsComponent(options: Map[Symbol, Any]) = {
	  options.get('mqSystem).getOrElse("").toString match {
	    case "webspheremq" => getWebsphereMqComponent(options)
	    case "activemq" => getActiveMqComponent(options)
	    case anythingelse: String => {
	      println(options.get('mySystem))
	      null
	    }
	  }
	}
	
	private def getActiveMqComponent(options: Map[Symbol, Any]) = {
		ActiveMQComponent.activeMQComponent(options.get('activeMqConnectionString).get.asInstanceOf[String])
	}
	
	private def getWebsphereMqComponent(options: Map[Symbol, Any]) = {
		val connectionFactory = this.getClass().getClassLoader().loadClass("com.ibm.mq.jms.MQConnectionFactory").newInstance()
		connectionFactory.getClass.getMethod("setTransportType", classOf[java.lang.Integer]).invoke(connectionFactory, 1.asInstanceOf[java.lang.Integer])
		connectionFactory.getClass.getMethod("setHostName", classOf[java.lang.String]).invoke(connectionFactory, options.get('websphereMqHostName).get.toString)
		connectionFactory.getClass.getMethod("setPort", classOf[java.lang.Integer]).invoke(connectionFactory, options.get('websphereMqPort).get.asInstanceOf[java.lang.Integer])
		connectionFactory.getClass.getMethod("setQueueManager", classOf[java.lang.String]).invoke(connectionFactory, options.get('websphereMqQueueManager).get.toString)
		connectionFactory.getClass.getMethod("setChannel", classOf[java.lang.String]).invoke(connectionFactory, options.get('websphereMqChannel).get.toString)
		val jmsComponent = new org.apache.camel.component.jms.JmsComponent
		jmsComponent.setConnectionFactory(connectionFactory.asInstanceOf[javax.jms.ConnectionFactory])
		jmsComponent
	}
}