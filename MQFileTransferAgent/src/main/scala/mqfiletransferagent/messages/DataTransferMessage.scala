package mqfiletransferagent.messages

import scala.xml.XML
import scala.xml.Elem

class DataTransferMessage(private val elem: Elem) {
  val command = (elem \ "type").text
  val transferid = (elem \ "transferid").text
  validate
  
  def validate{
    if (command == "" || transferid == "") throw new DataTransferMessageParseException
  }
}

object DataTransferMessage {
  def apply(message: String) = {
    new DataTransferMessage( XML.loadString(message) )
  }
}

class DataTransferMessageParseException extends RuntimeException