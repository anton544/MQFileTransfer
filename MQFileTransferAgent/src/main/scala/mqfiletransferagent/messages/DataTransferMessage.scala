package mqfiletransferagent.messages

import scala.xml.XML
import scala.xml.Elem

class DataTransferMessage(private val elem: Elem) {
  val command = (elem \ "type").text
  val transferid = (elem \ "transferid").text
  lazy val data = (elem \ "data").text
  lazy val segmentNumber = (elem \ "segmentnumber").text.toInt
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


case class FileData(data: String, filename: String, transferid: String, segmentNumber: Int)