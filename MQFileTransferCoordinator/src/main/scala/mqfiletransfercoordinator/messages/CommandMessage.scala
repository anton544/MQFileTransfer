package mqfiletransfercoordinator.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage (private val elem: Elem) {
  val command = (elem \ "type").text
  validate
  lazy val transferid = (elem \ "transferid").text
  lazy val status = (elem \ "status").text
  lazy val server = (elem \ "server").text
  lazy val commandQueueName = (elem \ "commandqueuename").text
  lazy val sourceServer = (elem \ "sourceserver").text
  lazy val sourcePath = (elem \ "sourcepath").text
  lazy val targetServer = (elem \ "targetserver").text
  lazy val targetPath = (elem \ "targetpath").text
  lazy val segmentNumber = (elem \ "segmentnumber").text.toInt
  lazy val segmentsTotal = (elem \ "segmentstotal").text.toInt
  
  def validate {
  	if (command == "") throw new CommandMessageParseException
  }
  
  override def toString() = {
	  "CommandMessage(" + elem.toString + ")"
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