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
import scala.annotation.tailrec
import mqfiletransferagent.messages.CommandMessage

object MqFileTransferAgent extends App {
	val usage = """
	  Usage: MqFileTransferAgent --mqSystem mqSystem --server server --commandq commandQueueName --dataq dataQueueName --coordinatorq coordinatorQueueName [--transferSize transferSize] [--websphereMqHostName hostname] [--websphereMqQueueManager QueueManager] [--websphereMqChannel Channel] [--websphereMqPort Port] [--activeMqConnectionString ConnectionString]
	"""
	  
	if (args.length == 0) {
	  println(usage)
	  sys.exit(-1)
	}
	
	@tailrec
	private def nextOption(map: Map[Symbol, Any], list: List[String]): Map[Symbol, Any] = {
	  list match {
	    case Nil => map
	    case "--server" :: server :: tail =>
	      nextOption(map ++ Map('server -> server), tail)
	    case "--commandq" :: commandQueueName :: tail =>
	      nextOption(map ++ Map('commandq -> commandQueueName), tail)
	    case "--dataq" :: dataQueueName :: tail =>
	      nextOption(map ++ Map('dataq -> dataQueueName), tail)
	    case "--coordinatorq" :: coordinatorQueueName :: tail =>
	      nextOption(map ++ Map('coordinatorq -> coordinatorQueueName), tail)
	    case "--transferSize" :: transferSize :: tail =>
	      nextOption(map ++ Map('transferSize -> transferSize.toInt), tail)
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
	  
	val options = nextOption(Map(), args.toList)
	val system = ActorSystem("MqFileTransferAgent")
	val camelExtension = CamelExtension(system)
	val camelContext = camelExtension.context
	camelContext.addComponent("mq", getJmsComponent(options))
	val coordinatorQueueProducer = system.actorOf(Props(new CoordinatorQueueProducer("mq:queue:" + options.getOrElse('coordinatorq, "COORDINATOR.COMMAND.QUEUE").asInstanceOf[String])), "coordinatorQueueProducer")
	val commandQueueProducer = system.actorOf(Props[CommandQueueProducer], "commandQueueProducer")
	val dataQueueProducer = system.actorOf(Props[DataQueueProducer], "dataQueueProducer")
	val fileActor = system.actorOf(Props(new FileActor(dataQueueProducer, None, coordinatorQueueProducer, options.getOrElse('transferSize, 1024 * 512).asInstanceOf[Int])), "fileActor")
	val agentTransferCoordinator = system.actorOf(Props(new AgentTransferCoordinator(dataQueueProducer, commandQueueProducer, fileActor, coordinatorQueueProducer)), "agentTransferCoordinator")
	val commandQueueConsumer = system.actorOf(Props(new CommandQueueConsumer("mq:queue:" + options.getOrElse('commandq, "AGENT.COMMAND.QUEUE").asInstanceOf[String], agentTransferCoordinator)), "commandQueueConsumer")
	val dataQueueConsumer = system.actorOf(Props(new DataQueueConsumer("mq:queue:" + options.getOrElse('dataq, "AGENT.DATA.QUEUE").asInstanceOf[String], agentTransferCoordinator)), "dataQueueConsumer")
	coordinatorQueueProducer ! CommandMessage(s"<message><type>AgentJoin</type><server>${options.get('server).get.asInstanceOf[String]}</server><commandqueuename>${options.getOrElse('commandq, "AGENT.COMMAND.QUEUE").asInstanceOf[String]}</commandqueuename><dataqueuename>${options.getOrElse('dataq, "AGENT.DATA.QUEUE").asInstanceOf[String]}</dataqueuename></message>")

	def shutdown() {
		system.shutdown
		Thread.sleep(5000)
		System.exit(0)
	}
	
	private def getJmsComponent(options: Map[Symbol, Any]) = {
	  options.get('mqSystem).getOrElse("").toString match {
	    case "webspheremq" => getWebsphereMqComponent(options)
	    case "activemq" => getActiveMqComponent(options)
	    case anythingelse: String => {
	      println(s"Invalid mqSystem: ${options.get('mySystem)}. Valid options are webspheremq or activemq")
	      System.exit(-1)
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