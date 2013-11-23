package mqfiletransfercoordinator.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage (private val elem: Elem) {
  val command = (elem \ "type").text
  validate
  lazy val transferid = (elem \ "transferid").text
  lazy val status = (elem \ "status").text
  
  def validate {
  	if (command == "") throw new CommandMessageParseException
  }
  
  override def equals(o: Any) = o match {
  	case that: CommandMessage => elem.equals(that.elem)
  	case _ => false
  }
  
  override def hashCode = elem.hashCode
  
  def toXmlString() = {
	  command match {
	      case _ => "<message><type>%s</type><transferid>%s</transferid></message>" format (command, transferid)
	  }
  }
}

object CommandMessage {
  def apply(message: String) = new CommandMessage( XML.loadString(message) )
}

class CommandMessageParseException extends RuntimeException