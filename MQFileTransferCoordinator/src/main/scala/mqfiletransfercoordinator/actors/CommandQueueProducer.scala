package mqfiletransfercoordinator.actors

import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.Actor
import akka.camel.Camel
import mqfiletransfercoordinator.messages.CommandMessage
import scala.collection.mutable.Map
import mqfiletransfercoordinator.messages.AddProducer
import mqfiletransfercoordinator.messages.RemoveProducer
import mqfiletransfercoordinator.messages.CancelTransfer
import mqfiletransfercoordinator.messages.TransferSuccessfulReply
import mqfiletransfercoordinator.messages.InitiateAgentTransferCommand
import mqfiletransfercoordinator.messages.TransferFailedReply
import mqfiletransfercoordinator.messages.TransferInitiated
import mqfiletransfercoordinator.messages.CancelTransferAgentCommand
import mqfiletransfercoordinator.messages.TransferQueryAck
import mqfiletransfercoordinator.MqFileTransferCoordinator
import mqfiletransfercoordinator.messages.ShutdownAgent

class CommandQueueProducer(camelExtension: Camel) extends Actor with ActorLogging {
  import CommandQueueProducer._
  val producerTemplate = camelExtension.template

  def receive = LoggingReceive {
    case reply: TransferSuccessfulReply =>
      log.debug("sending..." + reply.toString)
      producerTemplate.sendBody("mq:queue:" + reply.requestorQueueName, s"<message><type>TransferSuccessful</type><transferid>${reply.transferId}</transferid></message>")
    case reply: TransferFailedReply =>
      log.debug("sending..." + reply.toString)
      producerTemplate.sendBody("mq:queue:" + reply.requestorQueueName, s"<message><type>TransferFailed</type><transferid>${reply.transferId}</transferid></message>")
    case message: TransferInitiated =>
      log.debug("sending..." + message.toString)
      producerTemplate.sendBody("mq:queue:" + message.requestorQueueName, s"<message><type>TransferInitiated</type><transferid>${message.transferId}</transferid><correlationid>${message.correlationId}</correlationid></message>")
    case command: InitiateAgentTransferCommand =>
      log.debug("sending..." + command.toString)
      producerTemplate.sendBody("mq:queue:" + command.sourceCommandQueue, s"<message><type>InitiateTransfer</type><transferid>${command.transferId}</transferid><sourcepath>${command.sourcePath}</sourcepath><targetpath>${command.targetPath}</targetpath><sourcecommandqueue>${command.sourceCommandQueue}</sourcecommandqueue><sourcedataqueue>${command.sourceDataQueue}</sourcedataqueue><targetcommandqueue>${command.targetCommandQueue}</targetcommandqueue><targetdataqueue>${command.targetDataQueue}</targetdataqueue></message>")
    case command: CancelTransferAgentCommand =>
      log.debug("sending..." + command.toString)
      producerTemplate.sendBody("mq:queue:" + command.commandQueueName, s"<message><type>CancelTransfer</type><transferid>${command.transferId}</transferid></message>")
    case message: TransferQueryAck =>
      log.debug("sending..." + message.toString)
      producerTemplate.sendBody("mq:queue:" + message.queryReplyQueueName, s"<message><type>TransferQueryAck</type><transferid>${message.transferId}</transferid><status>${message.status}</status></message>")
    case message: ShutdownAgent =>
      log.debug("sending..." + message.toString)
      producerTemplate.sendBody("mq:queue:" + message.commandQueueName, "<message><type>Shutdown</type></message>")
    case SHUTDOWN => MqFileTransferCoordinator.shutdown
    case a: Any => log.warning("Unknown message type: " + a.getClass.toString)
  }
}

object CommandQueueProducer {
} 