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
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.RemoveProducer


@RunWith(classOf[JUnitRunner])
class CommandQueueProducerSpec extends TestKit(ActorSystem("CommandQueueProducerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}

//Akka-camel is keeping the actor from receiving the message.  Need to fix later
//	"A CommandQueueProducer receiving a AddProducer message" must {
//		"add queue lookup" in {
//			CommandQueueProducer.mailboxMap.clear
//			val actor = system.actorOf(Props[CommandQueueProducer])
//			actor ! AddProducer("1234", "AQUEUE")
//			Thread.sleep(100)
//			assert(CommandQueueProducer.mailboxMap.get("1234").isDefined)
//		}
//	}
//
//	"A CommandQueueProducer receiving a RemoveProducer message" must {
//		"remove queue lookup" in {
//			CommandQueueProducer.mailboxMap.clear
//			CommandQueueProducer.mailboxMap += ("1234" -> "AQUEUE")
//			val actor = system.actorOf(Props[CommandQueueProducer])
//			actor ! RemoveProducer("1234")
//			Thread.sleep(100)
//			assert(!CommandQueueProducer.mailboxMap.get("1234").isDefined)
//		}
//	}
}