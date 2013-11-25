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
import mqfiletransferagent.messages.CommandMessage
import akka.testkit.TestProbe
import mqfiletransferagent.messages.RemoveProducer
import mqfiletransferagent.messages.CleanupFile
import mqfiletransferagent.messages.FileWriteVerify
import mqfiletransferagent.messages.FileWriteSuccess
import mqfiletransferagent.messages.AddProducer
import mqfiletransferagent.messages.FileWriteFailure
import mqfiletransferagent.messages.DataTransferMessage
import mqfiletransferagent.messages.FileData
import mqfiletransferagent.messages.FileVerify
import mqfiletransferagent.messages.TransferNextSegment
import mqfiletransferagent.messages.FileReadFailure
import mqfiletransferagent.messages.FileReadVerify
import mqfiletransferagent.messages.FileReadSuccess

@RunWith(classOf[JUnitRunner])
class AgentTransferCoordinatorSpec extends TestKit(ActorSystem("AgentTransferCoordinatorSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	import AgentTransferCoordinatorSpec._
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
	
	"An AgentTransferCoordinator receiving a CancelTransfer message" must {
		"remove path lookup" in {
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val fileActorProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/a")
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! cancelTransferMessage
			Thread.sleep(100)
			assert(!AgentTransferCoordinator.pathMap.get("1234").isDefined)
		}
		
		"send a remove producer message to the DataProducerCoordinator and CmdProducerCoordinator" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val fileActorProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! cancelTransferMessage
			dataProducerProbe.expectMsgClass(100 millis, classOf[RemoveProducer])
			cmdProducerProbe.expectMsgClass(100 millis, classOf[RemoveProducer])
		}
		
		"send a CleanupFile message to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! cancelTransferMessage
			fileActorProbe.expectMsgClass(100 millis, classOf[CleanupFile])
		}
	}
	
	"An AgentTransferCoordinator receiving a StartTransfer message" must {
		"send a file write verify to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! startTransferMessage
			fileActorProbe.expectMsgClass(250 millis, classOf[FileWriteVerify])
		}
	}
	
	"An AgentTransferCoordinator receiving a FileWriteSuccess message" must {
		"send a add producer message to the DataProducerCoordinator and CmdProducerCoordinator" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileWriteSuccess("", "")
			dataProducerProbe.expectMsgClass(250 millis, classOf[AddProducer])
			cmdProducerProbe.expectMsgClass(250 millis, classOf[AddProducer])
		}
		
		"send a StartTransferAck message with success to the CmdProducer" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileWriteSuccess("1234", "/a")
			cmdProducerProbe.expectMsgClass(250 millis, classOf[AddProducer])
			cmdProducerProbe.expectMsg(250 millis, successfulStartTransferAck)
		}
		
		"add a path lookup" in {
			AgentTransferCoordinator.pathMap.clear
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileWriteSuccess("1234", "/a")
			Thread.sleep(100)
			assert(AgentTransferCoordinator.pathMap.get("1234").isDefined)
		}
	}
	
	"An AgentTransferCoordinator receiving a FileWriteFailure message" must {
		"send a StartTransferAck message with failure to the CmdProducer" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileWriteFailure("1234")
			cmdProducerProbe.expectMsg(100 millis, failureStartTransferAck)
		}
		"send a RemoveProducer message after the StartTransferAck message to the CmdProducer" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileWriteFailure("1234")
			cmdProducerProbe.expectMsgClass(100 millis, failureStartTransferAck.getClass)
			cmdProducerProbe.expectMsgClass(100 millis, classOf[RemoveProducer])
		}
	}
	
	"An AgentTransferCoordinator receiving a DataTransfer message" must {
		"send a FileData message to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferMessage
			fileActorProbe.expectMsgClass(100 millis, classOf[FileData])
		}
	}
	
	"An AgentTransferCoordinator receiving a DataTransferComplete message" must {
		"send a FileVerify message to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferComplete
			fileActorProbe.expectMsgClass(100 millis, classOf[FileVerify])
		}
	}
	
	"An AgentTransferCoordinator receiving an InitiateTransfer message" must {
		"send a FileReadVerify message to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! initiateTransfer 
			fileActorProbe.expectMsg(100 millis, FileReadVerify("1234", "/somefile", "/somefile2", "TARGET.COMMAND.QUEUE", "TARGET.DATA.QUEUE"))
		}
	}
	
	"An AgentTransferCoordinator receiving a FileReadSuccess message" must {
		"send a AddProducer message to the CommandQueueProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileReadSuccess("1234", "/somefile", "TARGET.COMMAND.QUEUE", "TARGET.DATA.QUEUE")
			cmdProducerProbe.expectMsg(100 millis, AddProducer("1234", "TARGET.COMMAND.QUEUE"))
		}
		
		"send a AddProducer message to the DataProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileReadSuccess("1234", "/somefile", "TARGET.COMMAND.QUEUE", "TARGET.DATA.QUEUE")
			dataProducerProbe.expectMsg(100 millis, AddProducer("1234", "TARGET.DATA.QUEUE"))
		}
		
		"send a StartTransfer message to the CommandQueueProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileReadSuccess("1234", "/somefile", "TARGET.COMMAND.QUEUE", "TARGET.DATA.QUEUE")
			cmdProducerProbe.expectMsgClass(100 millis, classOf[AddProducer])
			cmdProducerProbe.expectMsg(100 millis, startTransferMessage)
		}
	}
	
	"An AgentTransferCoordinator receiving a FileReadFailure message" must {
		"send a TransferFailure message to the CoordinatorQueueProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! FileReadFailure("1234")
			coordProducerProbe.expectMsg(100 millis, transferFailure)
		}
	}
	
	"An AgentTransferCoordinator receiving a DataTransferAck message" must {
		"send a TransferNextSegment message to the FileActor" in {
			AgentTransferCoordinator.pathMap.clear
			AgentTransferCoordinator.pathMap += ("1234" -> "/somefile")
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferAckMessage
			fileActorProbe.expectMsg(100 millis, TransferNextSegment("1234", "/somefile", 2))
		}
	}
	
	"An AgentTransferCoordinator receiving a DataTransferCompleteAck message" must {
		"send a RemoveProducer message to the CommandQueueProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferCompleteAckWithSuccess
			cmdProducerProbe.expectMsg(100 millis, RemoveProducer("1234"))
		}
		
		"send a RemoveProducer message to the DataQueueProducer" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferCompleteAckWithSuccess
			dataProducerProbe.expectMsg(100 millis, RemoveProducer("1234"))
		}
		
		"send a TransferSuccess message to the CoordinatorQueueProducer if the status was Success" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferCompleteAckWithSuccess
			coordProducerProbe.expectMsg(250 millis, transferSuccess)
		}
		
		"send a TransferFailure message to the CoordinatorQueueProducer if the status was Failure" in {
			val fileActorProbe = TestProbe()
			val dataProducerProbe = TestProbe()
			val cmdProducerProbe = TestProbe()
			val coordProducerProbe = TestProbe()
			val actor = system.actorOf(Props(new AgentTransferCoordinator(dataProducerProbe.ref, cmdProducerProbe.ref, fileActorProbe.ref, coordProducerProbe.ref)))
			actor ! dataTransferCompleteAckWithFailure
			coordProducerProbe.expectMsg(250 millis, transferFailure)
		}
	}
}

object AgentTransferCoordinatorSpec {
	val cancelTransferMessage = new CommandMessage(<message><type>CancelTransfer</type><transferid>1234</transferid></message>)
	val startTransferMessage = new CommandMessage(<message><type>StartTransfer</type><transferid>1234</transferid><targetpath>/somefile</targetpath><targetcommandqueue>TARGET.COMMAND.QUEUE</targetcommandqueue><targetdataqueue>TARGET.DATA.QUEUE</targetdataqueue></message>)
	val successfulStartTransferAck = new CommandMessage(<message><type>StartTransferAck</type><transferid>1234</transferid><status>Success</status></message>)
	val failureStartTransferAck = new CommandMessage(<message><type>StartTransferAck</type><transferid>1234</transferid><status>Fail</status></message>)
	val dataTransferMessage = new DataTransferMessage(<message><type>DataTransfer</type><transferid>1234</transferid><data>MUFyY2hJZDE=</data><segmentnumber>1</segmentnumber><segmentstotal>1</segmentstotal></message>)
	val dataTransferAckMessage = new DataTransferMessage(<message><type>DataTransferAck</type><transferid>1234</transferid><segmentnumber>1</segmentnumber></message>)
	val dataTransferComplete = new DataTransferMessage(<message><type>DataTransferComplete</type><transferid>1234</transferid></message>)
	val dataTransferCompleteAckWithFailure = new DataTransferMessage(<message><type>DataTransferCompleteAck</type><transferid>1234</transferid><status>Failure</status></message>)
	val dataTransferCompleteAckWithSuccess = new DataTransferMessage(<message><type>DataTransferCompleteAck</type><transferid>1234</transferid><status>Success</status></message>)
	val transferFailure = new CommandMessage(<message><type>TransferFailure</type><transferid>1234</transferid></message>)
	val transferSuccess = new CommandMessage(<message><type>TransferSuccess</type><transferid>1234</transferid></message>)
	val initiateTransfer = new CommandMessage(
		<message>
			<type>InitiateTransfer</type>
			<transferid>1234</transferid>
			<sourcepath>/somefile</sourcepath>
			<targetpath>/somefile2</targetpath>
			<targetcommandqueue>TARGET.COMMAND.QUEUE</targetcommandqueue>
			<targetdataqueue>TARGET.DATA.QUEUE</targetdataqueue>
		</message>
	)
}