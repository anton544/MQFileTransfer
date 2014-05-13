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
  lazy val status = (elem \ "status").text
  validate
  
  def validate{
	  if (command == "" || transferid == "") throw new DataTransferMessageParseException
  }
  
  def toXmlString() = {
	  (s"<message><type>${command}</type><transferid>${transferid}</transferid>") +
	  dataPart() +
	  segmentNumberPart() +
	  md5HashPart() +
	  segmentsTotalPart() +
	  statusPart() +
	  "</message>"
  }

  def dataPart() = {
  	  if (command == "DataTransfer") s"<data>${data}</data>" else ""
  }
  def segmentNumberPart() = {
	  if (command == "DataTransfer" || command == "DataTransferAck") s"<segmentnumber>${segmentNumber}</segmentnumber>" else ""
  }
  def md5HashPart() = {
	  if (command == "DataTransferComplete") s"<md5hash>${md5hash}</md5hash>" else ""
  }
  def segmentsTotalPart() = {
	  if (command == "DataTransfer") s"<segmentstotal>${segmentsTotal}</segmentstotal>" else ""
  }
  def statusPart() = {
	  if (command == "DataTransferAck" || command == "DataTransferCompleteAck") s"<status>${status}</status>" else ""
  }
  
  override def equals(o : Any) = o match {
  	case that: DataTransferMessage => that.elem.equals(elem)
  	case _ => false
  }
  
  override def hashCode() = elem.hashCode
  
  override def toString() = s"DataTransferMessage [${elem.toString}]"
}

object DataTransferMessage {
  def apply(message: String) = {
    new DataTransferMessage( XML.loadString(message) )
  }
}

class DataTransferMessageParseException extends RuntimeException


case class FileData(data: String, filename: String, transferid: String, segmentNumber: Int, segmentsTotal: Int)