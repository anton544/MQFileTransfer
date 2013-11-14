package mqfiletransferagent.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage(private val elem: Elem) {
  lazy val command = (elem \ "type").text 
}

object CommandMessage {
  def apply(message: String) = {
    new CommandMessage( XML.loadString(message) )
  }
}