package mqfiletransferagent.messages

import org.scalatest.WordSpecLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers


@RunWith(classOf[JUnitRunner])
class CommandMessageSpec extends WordSpec with Matchers {
	val validMessage = "<message><type>InitiateTransfer</type><transferid>1234</transferid></message>"
	
	"The Command Message Object" must {
		"return a InitiateTransferCommandMessage with type InitiateTransfer " in {
			CommandMessage(validMessage).command shouldBe "InitiateTransfer"
			intercept[CommandMessageParseException] {
				CommandMessage("<message><transferid>1234</transferid></message>")
			}
		}
		"have a transfer id" in {
			CommandMessage(validMessage).transferid shouldBe "1234"
			intercept[CommandMessageParseException] {
				CommandMessage("<message><type>InitiateTransfer</type></message>")
			}
		}
	}
}