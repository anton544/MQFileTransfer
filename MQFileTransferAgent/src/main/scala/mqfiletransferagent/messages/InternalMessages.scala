package mqfiletransferagent.messages

case class RemoveProducer(transferid: String)
case class AddProducer(transferid: String, queuename: String)
case class CleanupFile(path: String)
case class FileWriteVerify()
case class FileWriteSuccess(transferid: String, path: String)
case class FileWriteFailure(transferid: String)
case class FileVerify()
case class TransferProgress(transferid: String, segmentsComplete: Int, segmentsTotal: Int) {
	def toXmlString() = {
		"<message><type>TransferProgress</type><transferid>%s</transferid><segmentsComplete>%d</segmentsComplete><segmentsTotal>%d</segmentsTotal></message>" format (transferid, segmentsComplete, segmentsTotal)
	}
}