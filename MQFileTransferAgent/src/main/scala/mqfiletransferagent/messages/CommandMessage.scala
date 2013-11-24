package mqfiletransferagent.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage (private val elem: Elem) {
  val command = (elem \ "type").text
  val transferid = (elem \ "transferid").text
  validate
  lazy val status = (elem \ "status").text
  lazy val targetPath = (elem \ "targetpath").text
  lazy val sourcePath = (elem \ "sourcepath").text
  lazy val sourceQueue = (elem \ "sourcequeue").text
  lazy val targetCommandQueue = (elem \ "targetcommandqueue").text
  lazy val targetDataQueue = (elem \ "targetdataqueue").text
  
  def validate {
  	if (command == "" || transferid == "") throw new CommandMessageParseException
  }
  
  override def equals(o: Any) = o match {
  	case that: CommandMessage => elem.equals(that.elem)
  	case _ => false
  }
  
  override def hashCode = elem.hashCode
  
  def toXmlString() = {
	  command match {
	      case "StartTransferAck" => "<message><type>%s</type><transferid>%s</transferid><status>%s</status></message>" format (command, transferid, status)
	      case _ => "<message><type>%s</type><transferid>%s</transferid></message>" format (command, transferid)
	  }
  }
  override def toString() = "CommandMessage[" + elem + "]"
}

object CommandMessage {
  def apply(message: String) = new CommandMessage( XML.loadString(message) )
}

class CommandMessageParseException extends RuntimeException