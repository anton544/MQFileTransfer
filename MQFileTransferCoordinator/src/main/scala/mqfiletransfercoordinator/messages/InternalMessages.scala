package mqfiletransfercoordinator.messages

case class AddProducer(server: String, queueName: String)
case class AgentJoin(server: String, commandQueue: String, dataQueue: String)
case class AgentRemove(server: String)
case class CancelTransfer(transferid: String, server: String) {
	def toXmlString() = s"<message><type>CancelTransfer</type><transferid>${transferid}</transferid></message>"
}
case class CancelTransferRequest(transferid: String)
case class InitiateTransfer(requestorQueueName: String, correlationId: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String)
case class RemoveProducer(server: String)
case class ShutdownRequest()
case class ShutdownAgent(commandQueueName: String)
case class TransferFailure(transferid: String)
case class TransferInitiated(transferId: String, requestorQueueName: String, correlationId: String)
case class TransferProgress(transferid: String, segmentNumber: Int, segmentsTotal: Int)
case class TransferSuccess(transferid: String)
case class TransferQuery(transferid: String, queryReplyQueueName: String)
case class TransferQueryAck(transferid: String, status: String)