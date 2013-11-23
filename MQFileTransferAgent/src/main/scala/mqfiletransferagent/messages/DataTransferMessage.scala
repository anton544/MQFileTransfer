package mqfiletransferagent.messages

import scala.xml.XML
import scala.xml.Elem

class DataTransferMessage(val elem: Elem) {
  val command = (elem \ "type").text
  val transferid = (elem \ "transferid").text
  lazy val data = (elem \ "data").text
  lazy val segmentNumber = (elem \ "segmentnumber").text.toInt
  lazy val md5hash = (elem \ "md5hash").text
  lazy val segmentsTotal = (elem \ "segmentstotal").text.toInt
  validate
  
  def validate{
    if (command == "" || transferid == "") throw new DataTransferMessageParseException
  }
  
  override def equals(o : Any) = o match {
  	case that: DataTransferMessage => that.elem.equals(elem)
  	case _ => false
  }
  
  override def hashCode() = elem.hashCode
  
  override def toString() = "DataTransferMessage [" + elem.toString + "]"
}

object DataTransferMessage {
  def apply(message: String) = {
    new DataTransferMessage( XML.loadString(message) )
  }
}

class DataTransferMessageParseException extends RuntimeException


case class FileData(data: String, filename: String, transferid: String, segmentNumber: Int, segmentsTotal: Int)