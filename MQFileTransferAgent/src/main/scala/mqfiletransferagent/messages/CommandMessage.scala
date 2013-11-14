package mqfiletransferagent.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage (private val elem: Elem) {
  val command = (elem \ "type").text
  val transferid = (elem \ "transferid").text
  validate
  
  def validate {
    if (command == "" || transferid == "") throw new CommandMessageParseException
  }
}

object CommandMessage {
  def apply(message: String) = new CommandMessage( XML.loadString(message) )
}

class CommandMessageParseException extends RuntimeException