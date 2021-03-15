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
  private val eventDlqAwsSqsClient: AmazonSQS,
  @Value("\${offender-events.sqs-queue}") private val eventQueueUrl: String,
  @Value("\${offender-events.sqs-dlq}") private val eventDlqUrl: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferEventMessages() =
    repeat(1) {
      eventDlqAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventDlqUrl).withMaxNumberOfMessages(1)).messages
        .forEach { msg ->
          log.info("Got a message: $msg.body")
          eventAwsSqsClient.sendMessage(eventQueueUrl, msg.body)
          eventDlqAwsSqsClient.deleteMessage(DeleteMessageRequest(eventDlqUrl, msg.receiptHandle))
          log.info("Moved a message from the dlq to the main queue: $msg.receiptHandle")
        }
    }

  private fun getEventDlqMessageCount() =
    eventDlqAwsSqsClient.getQueueAttributes(eventDlqUrl, listOf("ApproximateNumberOfMessages"))
      .attributes["ApproximateNumberOfMessages"]
      ?.toInt() ?: 0
}
