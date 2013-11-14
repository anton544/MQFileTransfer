package mqfiletransferagent.messages

import org.scalatest.WordSpecLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers

@RunWith(classOf[JUnitRunner])
class DataTransferMessageSpec extends WordSpec with Matchers {
	val validMessage = "<message><type>CompletingTransfer</type><transferid>1234</transferid></message>"
	
	"The Data Transfer Message Object" must {
		"return a InitiateTransferCommandMessage with type CompletingTransfer " in {
			DataTransferMessage(validMessage).command shouldBe "CompletingTransfer"
			intercept[DataTransferMessageParseException] {
				DataTransferMessage("<message><transferid>1234</transferid></message>")
			}
		}
		"have a transfer id" in {
			DataTransferMessage(validMessage).transferid shouldBe "1234"
			intercept[DataTransferMessageParseException] {
				DataTransferMessage("<message><type>InitiateTransfer</type></message>")
			}
		}
	}
}