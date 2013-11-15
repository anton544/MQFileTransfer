package mqfiletransferagent.actors

import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.actor.ActorSystem
import akka.pattern.ask
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.concurrent.Await
import mqfiletransferagent.messages.FileData
import mqfiletransferagent.messages.DataTransferMessage
import org.apache.commons.codec.binary.Base64
import java.io.File
import akka.util.Timeout

@RunWith(classOf[JUnitRunner])
class FileActorSpec extends TestKit(ActorSystem("FileActorSpec")) 
with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
	override def afterAll {
		TestKit.shutdownActorSystem(system)
	}
	implicit val timeout = Timeout(250 millis)
	
	"A ByteArrayWritingActor" must {
		"acknowledge the data transfer" in {
		    val actor = system.actorOf(Props[ByteArrayWritingActor])
		    actor ! FileData("", "/dev/null", "1234", 1)
		    expectMsgClass(250 millis, classOf[DataTransferMessage])
		}
		"write data to file" in {
			val actor = system.actorOf(Props[ByteArrayWritingActor])
			val tempFile = File.createTempFile("deleteme", "test")
			tempFile.deleteOnExit()
			val data = FileData(Base64.encodeBase64String("Hello World".getBytes()), tempFile.getAbsolutePath(), "1234", 1)
			val future = actor ? data
			Await.result(future, timeout.duration)
			assert(tempFile.length > 0)
		}
	}
}