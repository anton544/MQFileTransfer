package mqfiletransferagent.messages

import scala.xml.XML

class DataTransferMessage
object DataTransferMessage {
  def apply(message: String) = {
    val xml = XML.loadString(message)
	(xml \\ "type").text match {
	  case "" => ()
	}
  }
}

case class CompletingTransferDataMessage extends DataTransferMessage
case class CompletingTransferAckDataMessage extends DataTransferMessage
case class DataTransferAckDataMessage extends DataTransferMessage
case class DataTransferDataMessage extends DataTransferMessage