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

@RunWith(classOf[JUnitRunner])
class TransferCoordinatorSpec extends TestKit(ActorSystem("AgentCoordinatorSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	import TransferCoordinatorSpec._
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
	
	"A TransferCoordinator receiving an InitiateTransfer message" must {
		"send the InitiateTransfer message to the commandProducer" in {
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! initiateTransfer
			val message = agentCommandProducer.receiveOne(100 millis).asInstanceOf[CommandMessage]
			assert(message.command == "InitiateTransfer")
		}
		"record that the Transfer is pending" in {
			val agentCommandProducer = new TestProbe(system) {
				def expectUpdate() {
					val message = this.receiveOne(100 millis).asInstanceOf[CommandMessage]
					println(message)
					assert(TransferCoordinator.transferMap.get(message.transferid).get == TransferRecord(message.transferid, "here.domain", "/file1", "there.domain", "/file2", "TransferInitiated"))
				}
			}
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! initiateTransfer
			agentCommandProducer.expectUpdate
		}
		"reply back with the Transfer's ID" in {
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! initiateTransfer
			val reply = receiveOne(100 millis)
			reply match {
				case message: CommandMessage => {
				  assert(message.command == "TransferInitiated")
				}
				case _ => assert(false)
			}
		}
	}
	"A TransferCoordinator receiving a CancelTransfer message" must {
		"send a CancelTransfer message to the CommandProducer with the source" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>CancelTransfer</type><transferid>1234</transferid></message>)
			agentCommandProducer.expectMsg(100 millis, CancelTransfer("1234", "sourceserver"))
		}
		"send a CancelTransfer message to the CommandProducer with the target" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>CancelTransfer</type><transferid>1234</transferid></message>)
			agentCommandProducer.receiveOne(100 millis)
			agentCommandProducer.expectMsg(100 millis, CancelTransfer("1234", "targetserver"))
		}
		"record that the Transfer is canceled" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "", "targetserver", "", ""))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>CancelTransfer</type><transferid>1234</transferid></message>)
			agentCommandProducer.receiveN(2, 100 millis)
			assert(TransferCoordinator.transferMap.get("1234").get.status == "Cancelled")
		}
	}
	"A TransferCoordinator receiving a TransferStatus message" must {
		"record that the Transfer is progessing" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>TransferStatus</type><transferid>1234</transferid><segmentnumber>1</segmentnumber><segmentstotal>2</segmentstotal></message>)
			Thread.sleep(100)
			assert(TransferCoordinator.transferMap.get("1234").get.status == "In Progess(1/2)")
		}
	}
	"A TransferCoordinator receiving a TransferSuccess message" must {
		"record that the Transfer is completed successfully" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>TransferSuccess</type><transferid>1234</transferid></message>)
			Thread.sleep(100)
			assert(TransferCoordinator.transferMap.get("1234").get.status == "Success")
		}
	}
	"A TransferCoordinator receiving a TransferFailure message" must {
		"record that the Transfer has failed" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
			val agentCommandProducer = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor ! new CommandMessage(<message><type>TransferFailure</type><transferid>1234</transferid></message>)
			Thread.sleep(100)
			assert(TransferCoordinator.transferMap.get("1234").get.status == "Failed")
		}
	}
	"A TransferCoordinator receiving a TransferQuery message" must {
		"reply with the Transfers Status" in {
			TransferCoordinator.transferMap.clear
			TransferCoordinator.transferMap += ("1234" -> TransferRecord("1234", "sourceserver", "sourcepath", "targetserver", "targetpath", "Transfer Pending"))
			val agentCommandProducer = TestProbe()
			val replyProbe = TestProbe()
			val actor = system.actorOf(Props(new TransferCoordinator(agentCommandProducer.ref)))
			actor.tell(new CommandMessage(<message><type>TransferQuery</type><transferid>1234</transferid></message>), replyProbe.ref)
			replyProbe.expectMsg(100 millis, new CommandMessage(<message><type>TransferQueryAck</type><transferid>1234</transferid><status>Transfer Pending</status></message>))
		}
	}
}

object TransferCoordinatorSpec {
	val initiateTransfer = new CommandMessage(<message><type>InitiateTransfer</type><sourceserver>here.domain</sourceserver><targetserver>there.domain</targetserver><sourcepath>/file1</sourcepath><targetpath>/file2</targetpath></message>)
}