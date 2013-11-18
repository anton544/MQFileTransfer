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
import akka.camel.CamelMessage

@RunWith(classOf[JUnitRunner])
class CommandQueueConsumerSpec extends TestKit(ActorSystem("CommandQueueConsumerSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
import CommandQueueConsumerSpec._
	
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
}

object CommandQueueConsumerSpec {
//  val commandMessage = new CommandMessage(<message><type>StartTransfer</type><transferid>1234</transferid></message>)
}