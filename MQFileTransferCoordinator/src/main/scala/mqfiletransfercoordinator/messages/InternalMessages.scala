package mqfiletransfercoordinator.messages

case class AddProducer(server: String, queueName: String)
case class RemoveProducer(server: String)
case class CancelTransfer(transferid: String, server: String) {
	def toXmlString() = "<message><type>CancelTransfer</type><transferid>%s</transferid></message>" format transferid
}