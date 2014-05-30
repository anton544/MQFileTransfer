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
import mqfiletransfercoordinator.messages.CancelTransfer
import mqfiletransfercoordinator.messages.CancelTransferRequest
import mqfiletransfercoordinator.messages.InitiateTransfer
import mqfiletransfercoordinator.messages.CancelTransferCommand
import mqfiletransfercoordinator.messages.CancelTransferAgentCommand
import mqfiletransfercoordinator.messages.InitiateAgentTransferCommand
import mqfiletransfercoordinator.messages.InitiateAgentTransfer

@RunWith(classOf[JUnitRunner])
class AgentCoordinatorSpec extends TestKit(ActorSystem("AgentCoordinatorSpec"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  import AgentCoordinatorSpec._
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An AgentCoordinator receiving an AgentJoinCommand message" must {
    "create an agent record" in {
      AgentCoordinator.agentMap.clear
      val commandProducer = TestProbe()
      val actor = system.actorOf(Props(new AgentCoordinator(commandProducer.ref)))
      actor ! agentJoin
      commandProducer.expectNoMsg(100 millis) //time delay
      assert(AgentCoordinator.agentMap.size == 1)
      assert(AgentCoordinator.agentMap.get("someplace.somewhere").get == AgentRecord("someplace.somewhere", "AGENT.COMMAND.QUEUE", "AGENT.DATA.QUEUE"))
    }
  }

  "An AgentCoordinator receiving an AgentRemoveCommand message" must {
    "remove that agent's record" in {
      AgentCoordinator.agentMap.clear
      AgentCoordinator.agentMap += ("someplace.somewhere" -> AgentRecord("someplace.somewhere", "AGENT.COMMAND.QUEUE", "AGENT.DATA.QUEUE"))
      val commandProducer = TestProbe()
      val actor = system.actorOf(Props(new AgentCoordinator(commandProducer.ref)))
      actor ! agentRemove
      commandProducer.expectNoMsg(100 millis) //time delay
      assert(AgentCoordinator.agentMap.size == 0)
    }
  }

  "An AgentCoordinator receiving a Shutdown message" must {
    "send a Shutdown message per agent record to the AgentCommandProducer" in {
      AgentCoordinator.agentMap.clear
      AgentCoordinator.agentMap += ("server" -> AgentRecord("server", "commandQueue", "dataQueue"))
      AgentCoordinator.agentMap += ("server2" -> AgentRecord("server2", "commandQueue2", "dataQueue2"))
      val agentCommandProbe = TestProbe()
      val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
      actor ! ShutdownRequest()
      agentCommandProbe.expectMsg(100 millis, ShutdownAgent("commandQueue2"))
      agentCommandProbe.expectMsg(100 millis, ShutdownAgent("commandQueue"))
      agentCommandProbe.expectMsg(100 millis, SHUTDOWN)
      agentCommandProbe.expectNoMsg(100 millis)
    }
  }

  "An AgentCoordinator receiving a CancelTransferCommand message" must {
    "send a CancelTransferAgentCommand message to the command producer for the for both agents" in {
      AgentCoordinator.agentMap.clear
      AgentCoordinator.agentMap += ("server" -> AgentRecord("server", "commandQueue", "dataQueue"))
      AgentCoordinator.agentMap += ("server2" -> AgentRecord("server2", "commandQueue2", "dataQueue2"))
      val agentCommandProbe = TestProbe()
      val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
      actor ! cancelTransfer
      agentCommandProbe.expectMsg(100 millis, CancelTransferAgentCommand("commandQueue", "1234"))
      agentCommandProbe.expectMsg(100 millis, CancelTransferAgentCommand("commandQueue2", "1234"))
    }
  }

  "An AgentCoordinator receiving a InitiateTransfer message" must {
    "send an InitiateTransferCommand message to the command producer destined for the source agent" in {
      AgentCoordinator.agentMap.clear
      AgentCoordinator.agentMap += ("server" -> AgentRecord("server", "commandQueue", "dataQueue"))
      AgentCoordinator.agentMap += ("server2" -> AgentRecord("server2", "commandQueue2", "dataQueue2"))
      val agentCommandProbe = TestProbe()
      val actor = system.actorOf(Props(new AgentCoordinator(agentCommandProbe.ref)))
      actor ! initiateTransfer
      agentCommandProbe.expectMsg(100 millis, InitiateAgentTransferCommand("1234","commandQueue", "dataQueue", "/here", "commandQueue2", "dataQueue2", "/there"))
    }
  }
}

object AgentCoordinatorSpec {
  val agentJoin = AgentJoin("someplace.somewhere", "AGENT.COMMAND.QUEUE", "AGENT.DATA.QUEUE")
  val agentRemove = AgentRemove("someplace.somewhere")
  val cancelTransfer = CancelTransferCommand("server", "server2", "1234")
  val initiateTransfer = InitiateAgentTransfer("1234", "server", "/here", "server2", "/there")
}