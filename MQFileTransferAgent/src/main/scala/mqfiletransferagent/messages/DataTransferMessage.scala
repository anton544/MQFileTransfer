package mqfiletransferagent.messages

import scala.xml.XML
import scala.xml.Elem

class DataTransferMessage(private val elem: Elem) {
  lazy val command = (elem \ "type").text
}

object DataTransferMessage {
  def apply(message: String) = {
    new DataTransferMessage( XML.loadString(message) )
  }
}