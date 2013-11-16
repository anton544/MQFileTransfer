package mqfiletransferagent.messages

case class RemoveProducer(transferid: String)
case class AddProducer(transferid: String, queuename: String)
case class CleanupFile(path: String)
case class FileWriteVerify()
case class FileWriteSuccess(transferid: String, path: String)
case class FileWriteFailure(transferid: String)
case class FileVerify()