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
  
  override def equals(o: Any) = o match {
  	case that: CommandMessage => elem.equals(that.elem)
  	case _ => false
  }
  
  override def hashCode = elem.hashCode
}

object CommandMessage {
  def apply(message: String) = new CommandMessage( XML.loadString(message) )
}

class CommandMessageParseException extends RuntimeException