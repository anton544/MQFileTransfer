package mqfiletransferagent.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage

object CommandMessage {
  def apply(message: String) = {
    val xml = XML.loadString(message)
	(xml \\ "type").text match {
	  case "InitiateTransfer" => InitiateTransferCommandMessage()
	  case "StartingTransfer" => StartingTransferCommandMessage()
	  case "StartingTransferAck" => StartingTransferAckCommandMessage()
	}
  }
}

case class InitiateTransferCommandMessage extends CommandMessage
case class StartingTransferCommandMessage extends CommandMessage
case class StartingTransferAckCommandMessage extends CommandMessage