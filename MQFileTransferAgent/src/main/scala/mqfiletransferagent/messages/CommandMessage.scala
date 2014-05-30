package mqfiletransferagent.messages

import scala.xml.Elem
import scala.xml.XML

class CommandMessage (private val elem: Elem) {
  val command = (elem \ "type").text
  validate
  lazy val transferid = (elem \ "transferid").text
  lazy val status = (elem \ "status").text
  lazy val targetPath = (elem \ "targetpath").text
  lazy val sourcePath = (elem \ "sourcepath").text
  lazy val targetCommandQueue = (elem \ "targetcommandqueue").text
  lazy val targetDataQueue = (elem \ "targetdataqueue").text
  lazy val sourceCommandQueue = (elem \ "sourcecommandqueue").text
  lazy val sourceDataQueue = (elem \ "sourcedataqueue").text
  
  def validate {
  	if (command == "") throw new CommandMessageParseException
  }
  
  override def equals(o: Any) = o match {
  	case that: CommandMessage => elem.equals(that.elem)
  	case _ => false
  }
  
  override def hashCode = elem.hashCode
  
  def toXmlString() = {
	  elem.toString
  }
  override def toString() = "CommandMessage[" + elem + "]"
}

object CommandMessage {
  def apply(message: String) = new CommandMessage( XML.loadString(message) )
}

class CommandMessageParseException extends RuntimeException