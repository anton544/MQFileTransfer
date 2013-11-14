package mqfiletransferagent.messages

import org.scalatest.WordSpecLike
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers


@RunWith(classOf[JUnitRunner])
class CommandMessageSpec extends WordSpec with Matchers {
	"The Command Message Object" must {
		"return a InitiateTransferCommandMessage with type InitiateTransfer " in {
			CommandMessage("<message><type>InitiateTransfer</type></message>").command shouldBe "InitiateTransfer"
		}
	}
}