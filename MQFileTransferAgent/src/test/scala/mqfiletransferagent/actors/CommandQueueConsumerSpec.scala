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


@RunWith(classOf[JUnitRunner])
class CommandQueueConsumerSpec extends TestKit(ActorSystem("CommandQueueConsumerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}

	"A CommandQueueConsumer" must {
		"log when it recieves a message other than a Camel Message" in {
			val consumer = system.actorOf(Props(new CommandQueueConsumer("")))
			consumer ! "test"
			expectNoMsg(250 millis)
		}
	}
}