package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QueueAdminService(
  private val eventAwsSqsClient: AmazonSQS,
  private val eventAwsSqsDlqClient: AmazonSQS,
  @Value("\${offender-events.sqs-queue}") private val eventQueueUrl: String,
  @Value("\${offender-events-dlq.sqs-queue}") private val eventDlqUrl: String

) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferMessages() =
    repeat(getEventDlqMessageCount()) {
      eventAwsSqsDlqClient.receiveMessage(ReceiveMessageRequest(eventDlqUrl).withMaxNumberOfMessages(1)).messages
        .forEach { msg ->
          eventAwsSqsClient.sendMessage(eventQueueUrl, msg.body)
          eventAwsSqsDlqClient.deleteMessage(DeleteMessageRequest(eventDlqUrl, msg.receiptHandle))
        }
    }

  private fun getEventDlqMessageCount(): Int {

    val dlqMessageCount = eventAwsSqsDlqClient.getQueueAttributes(eventDlqUrl, listOf("ApproximateNumberOfMessages"))
      .attributes["ApproximateNumberOfMessages"]
      ?.toInt() ?: 0
    log.info("Transferring $dlqMessageCount from $eventDlqUrl to $eventQueueUrl")
    return dlqMessageCount
  }
}