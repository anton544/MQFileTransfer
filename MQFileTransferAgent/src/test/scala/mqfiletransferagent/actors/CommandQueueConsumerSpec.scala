package mqfiletransferagent.actors

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
import akka.camel.CamelMessage
import mqfiletransferagent.messages.CommandMessage


@RunWith(classOf[JUnitRunner])
class CommandQueueConsumerSpec extends TestKit(ActorSystem("CommandQueueConsumerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	import CommandQueueConsumerSpec._
	
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
//  CommandQueueConsumer is a camel consumer actor and is not being passed messages like regular actors for testing 
//	"A CommandQueueConsumer receiving an CamelMessage" must {
//		"send a CommandMessage to the AgentCoordinator" in {
//			val agentCoordProbe = TestProbe()
//			val consumer = system.actorOf(Props(new CommandQueueConsumer("asdf", agentCoordProbe.ref)))
//			consumer ! new CamelMessage("<message><type>StartTransfer</type><transferid>1234</transferid></message>", Map[String, Any]())
//			agentCoordProbe.expectMsg(100 millis, commandMessage)
//		}
//	}
}

object CommandQueueConsumerSpec {
  val commandMessage = new CommandMessage(<message><type>StartTransfer</type><transferid>1234</transferid></message>)
}