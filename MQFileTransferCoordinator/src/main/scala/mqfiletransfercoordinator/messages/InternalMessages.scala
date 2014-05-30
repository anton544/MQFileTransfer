package mqfiletransfercoordinator.messages

case class AddProducer(server: String, queueName: String)
case class AgentJoin(server: String, commandQueue: String, dataQueue: String)
case class AgentRemove(server: String)
case class CancelTransfer(transferId: String, server: String) {
	def toXmlString() = s"<message><type>CancelTransfer</type><transferid>${transferId}</transferid></message>"
}
case class CancelTransferRequest(transferId: String)
case class CancelTransferCommand(sourceServer: String, targetServer: String, transferId: String)
case class CancelTransferAgentCommand(commandQueueName: String, transferId: String)
case class InitiateTransfer(requestorQueueName: String, correlationId: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String)
case class InitiateAgentTransfer(transferId: String, sourceServer: String, sourcePath: String, targetServer: String, targetPath: String)
case class InitiateAgentTransferCommand(transferId: String, sourceCommandQueue: String, sourceDataQueue:String, sourcePath: String, targetCommandQueue: String, targetDataQueue: String, targetPath: String)
case class RemoveProducer(server: String)
case class ShutdownRequest()
case class ShutdownAgent(commandQueueName: String)
case class TransferFailure(transferId: String)
case class TransferInitiated(transferId: String, requestorQueueName: String, correlationId: String)
case class TransferProgress(transferId: String, segmentNumber: Int, segmentsTotal: Int)
case class TransferSuccess(transferId: String)
case class TransferQuery(transferId: String, queryReplyQueueName: String)
case class TransferQueryAck(queryReplyQueueName: String, transferId: String, status: String)
case class TransferSuccessfulReply(requestorQueueName: String, transferId: String)
case class TransferFailedReply(requestorQueueName: String, transferId: String)