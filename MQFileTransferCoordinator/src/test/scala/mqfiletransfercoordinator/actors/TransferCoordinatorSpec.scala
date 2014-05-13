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
import mqfiletransfercoordinator.messages.CancelTransfer
import mqfiletransfercoordinator.messages.InitiateTransfer
import mqfiletransfercoordinator.messages.CancelTransferRequest
import mqfiletransfercoordinator.messages.TransferProgress
import mqfiletransfercoordinator.messages.TransferSuccess
import mqfiletransfercoordinator.messages.TransferFailure
import mqfiletransfercoordinator.messages.TransferQuery
import mqfiletransfercoordinator.messages.TransferQueryAck
import mqfiletransfercoordinator.messages.TransferInitiated
import mqfiletransfercoordinator.messages.TransferInitiated

@RunWith(classOf[JUnitRunner])
class TransferCoordinatorSpec extends TestKit(ActorSystem("AgentCoordinatorSpec"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  import TransferCoordinatorSpec._
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A TransferCoordinator receiving an InitiateTransfer message" must {
    "send the InitiateTransfer message to the agent coordinator" in {
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! initiateTransfer
      val message = agentCoodinatorProbe.receiveOne(100 millis).asInstanceOf[InitiateTransfer]
    }
    "record that the Transfer is pending" in {
      val agentCoodinatorProbe = TestProbe()
      val agentCommandProducer = new TestProbe(system) {
        def expectUpdate() {
          val message = this.receiveOne(100 millis).asInstanceOf[TransferInitiated]
          //TODO:  println(message)
          assert(TransferCoordinator.transferMap.get(message.transferId).get == TransferRecord(message.transferId, "here.domain", "/file1", "there.domain", "/file2", "TransferInitiated"))
        }
      }
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! initiateTransfer
      agentCommandProducer.expectUpdate
    }
    "reply back to the requestor's queue with the Transfer's ID and correlation id" in {
      val agentCommandProducer = new TestProbe(system) {
        def expectation() {
          val message = this.receiveOne(100 millis).asInstanceOf[TransferInitiated]
          assert(message.requestorQueueName == initiateTransfer.requestorQueueName)
          assert(message.correlationId == initiateTransfer.correlationId)
          assert(message.transferId.size > 0)
        }
      }
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! initiateTransfer
      agentCommandProducer.expectation
    }
  }
  "A TransferCoordinator receiving a CancelTransferRequest message" must {
    "send a CancelTransfer message to the CommandProducer with the source" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! CancelTransferRequest("1234")
      agentCoodinatorProbe.expectMsg(100 millis, CancelTransfer("1234", "sourceserver"))
    }
    "send a CancelTransfer message to the CommandProducer with the target" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! CancelTransferRequest("1234")
      agentCoodinatorProbe.receiveOne(100 millis)
      agentCoodinatorProbe.expectMsg(100 millis, CancelTransfer("1234", "targetserver"))
    }
    "record that the Transfer is canceled" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! CancelTransferRequest("1234")
      agentCoodinatorProbe.receiveN(2, 100 millis)
      assert(TransferCoordinator.transferMap.get("1234").get.status == "Cancelled")
    }
  }
  "A TransferCoordinator receiving a TransferStatus message" must {
    "record that the Transfer is progessing" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! TransferProgress("1234", 1, 2)
      Thread.sleep(100)
      assert(TransferCoordinator.transferMap.get("1234").get.status == "In Progess(1/2)")
    }
  }
  "A TransferCoordinator receiving a TransferSuccess message" must {
    "record that the Transfer is completed successfully" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! TransferSuccess("1234")
      Thread.sleep(100)
      assert(TransferCoordinator.transferMap.get("1234").get.status == "Success")
    }
  }
  "A TransferCoordinator receiving a TransferFailure message" must {
    "record that the Transfer has failed" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! TransferFailure("1234")
      Thread.sleep(100)
      assert(TransferCoordinator.transferMap.get("1234").get.status == "Failed")
    }
  }
  "A TransferCoordinator receiving a TransferQuery message" must {
    "reply with the Transfers Status" in {
      TransferCoordinator.transferMap.clear
      TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
      val agentCommandProducer = TestProbe()
      val agentCoodinatorProbe = TestProbe()
      val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref, agentCoodinatorProbe.ref)))
      actor ! TransferQuery("1234", "REPLY.QUEUE.NAME")
      agentCommandProducer.expectMsg(100 millis, TransferQueryAck("1234", "Transfer Pending"))
    }
  }
}

object TransferCoordinatorSpec {
  val initiateTransfer = InitiateTransfer("EXTERNAL.QUEUE.NAME", "corrId", "here.domain", "/file1", "there.domain", "/file2")
}