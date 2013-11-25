package mqfiletransfercoordinator.actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import scala.concurrent.duration._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestProbe
import mqfiletransfercoordinator.messages.CommandMessage
import mqfiletransfercoordinator.messages.AddProducer
import mqfiletransfercoordinator.messages.RemoveProducer

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
			val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
			actor ! agentJoin
			agentCommandProbe.expectMsg(100 millis, AddProducer("someplace.somewhere", "AGENT.COMMAND.QUEUE"))
		}
	}
	
	"An AgentCoordinator receiving an AgentRemoveCommand message" must {
		"send an AgentRemove message to the AgentCommandProducer" in {
			val agentCommandProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
			actor ! agentRemove
			agentCommandProbe.expectMsg(100 millis, RemoveProducer("someplace.somewhere"))
		}
	}
	
	"An AgentCoordinator receiving a Shutdown message" must {
		
	}
}

object AgentCoordinatorSpec {
	val agentJoin = new CommandMessage(<message><type>AgentJoin</type><server>someplace.somewhere</server><commandqueuename>AGENT.COMMAND.QUEUE</commandqueuename></message>)
	val agentRemove = new CommandMessage(<message><type>AgentRemove</type><server>someplace.somewhere</server></message>)
}