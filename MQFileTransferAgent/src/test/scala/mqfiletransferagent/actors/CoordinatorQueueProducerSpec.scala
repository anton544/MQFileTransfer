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

class CoordinatorQueueProducerSpec extends TestKit(ActorSystem("CoordinatorQueueProducerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
	
	//akka-camel is keeping the actor from receiving messages in tests
}