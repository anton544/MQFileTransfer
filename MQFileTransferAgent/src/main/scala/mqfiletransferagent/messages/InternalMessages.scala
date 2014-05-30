package mqfiletransferagent.messages

case class RemoveProducer(transferid: String)
case class AddProducer(transferid: String, queuename: String)
case class CleanupFile(path: String)
case class FileWriteVerify(transferid: String, path: String, sourceDataQueue: String)
case class FileReadVerify(transferid: String, sourcePath: String, targetPath: String, sourceCommandQueue: String, sourceDataQueue: String, targetCommandQueue: String, targetDataQueue: String)
case class FileWriteSuccess(transferid: String, path:String, sourceDataQueue: String)
case class FileReadSuccess(transferid: String, sourcePath: String, targetPath: String, sourceCommandQueue: String, sourceDataQueue: String, targetCommandQueue: String, targetDataQueue: String)
case class FileWriteFailure(transferid: String)
case class FileReadFailure(transferid: String)
case class FileVerify(transferid: String, path: String, md5hash: String)
case class TransferProgress(transferid: String, segmentsComplete: Int, segmentsTotal: Int) {
	def toXmlString() = s"<message><type>TransferProgress</type><transferid>${transferid}</transferid><segmentnumber>${segmentsComplete}</segmentnumber><segmentstotal>${segmentsTotal}</segmentstotal></message>"
}
case class TransferNextSegment(transferid: String, path: String, nextSegmentNumber: Int)