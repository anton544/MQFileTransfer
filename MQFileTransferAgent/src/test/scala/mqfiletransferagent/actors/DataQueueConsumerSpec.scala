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
class DataQueueConsumerSpec extends TestKit(ActorSystem("DataQueueConsumerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
}