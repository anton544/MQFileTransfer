package mqfiletransfercoordinator.actors

import scala.concurrent.duration.DurationInt
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import mqfiletransfercoordinator.messages.AgentJoin
import mqfiletransfercoordinator.messages.AgentRemove
import mqfiletransfercoordinator.messages.ShutdownAgent
import mqfiletransfercoordinator.messages.ShutdownRequest
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AgentCoordinatorSpec extends TestKit(ActorSystem("AgentCoordinatorSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	import AgentCoordinatorSpec._
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
	
	"An AgentCoordinator receiving an AgentJoinCommand message" must {
		"send an AgentJoin message to the AgentCommandProducer" in {
			val agentCommandProbe = TestProbe()
			AgentCoordinator.agentMap.clear
			val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
			actor ! agentJoin
			agentCommandProbe.expectNoMsg(100 millis)
			assert(AgentCoordinator.agentMap.size == 1)
		}
	}
	
	"An AgentCoordinator receiving an AgentRemoveCommand message" must {
		"send an AgentRemove message to the AgentCommandProducer" in {
			val agentCommandProbe = TestProbe()
			AgentCoordinator.agentMap.clear
			AgentCoordinator.agentMap += ("someplace.somewhere" -> AgentRecord("someplace.somewhere", "commandQueue", "dataQueue"))
			val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
			actor ! agentRemove
			agentCommandProbe.expectNoMsg(100 millis)
			assert(AgentCoordinator.agentMap.size == 0)
		}
	}
	
	"An AgentCoordinator receiving a Shutdown message" must {
		"send a Shutdown message per agent record to the AgentCommandProducer" in {
			AgentCoordinator.agentMap += ("server" -> AgentRecord("server", "commandQueue", "dataQueue"))
			AgentCoordinator.agentMap += ("server2" -> AgentRecord("server2", "commandQueue2", "dataQueue2"))
			val agentCommandProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
			actor ! ShutdownRequest()
			agentCommandProbe.expectMsg(100 millis, ShutdownAgent("commandQueue2"))
			agentCommandProbe.expectMsg(100 millis, ShutdownAgent("commandQueue"))
			agentCommandProbe.expectNoMsg(100 millis)
		}
	}
	
	"An AgentCoordinator receiving a CancelRequest message" must {
		
	}
	
	"An AgentCoordinator receiving a InitiateTransfer message" must {
	  
	}
}

object AgentCoordinatorSpec {
	val agentJoin = AgentJoin("someplace.somewhere", "AGENT.COMMAND.QUEUE", "AGENT.DATA.QUEUE") 
	val agentRemove = AgentRemove("someplace.somewhere")
}