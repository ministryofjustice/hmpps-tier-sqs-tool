package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.google.gson.Gson
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QueueAdminService(
  private val eventAwsSqsClient: AmazonSQS,
  private val eventAwsSqsDlqClient: AmazonSQS,
  private val gson: Gson,
  private val telemetryClient: TelemetryClient,
  @Value("\${offender-events.sqs-queue}") private val eventQueueUrl: String,
  @Value("\${offender-events-dlq.sqs-queue}") private val eventDlqUrl: String

) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferMessages() {
    val dlqMessageCount = getEventDlqMessageCount().also { log.info("Transferring $it from $eventDlqUrl to $eventQueueUrl") }

    repeat(dlqMessageCount) {
      eventAwsSqsDlqClient.receiveMessage(ReceiveMessageRequest(eventDlqUrl).withMaxNumberOfMessages(1)).messages
        .forEach { msg ->
          val message = gson.fromJson(msg.body, SQSMessage::class.java).Message
          val crn = gson.fromJson(message, TierChangeEvent::class.java).crn
          telemetryClient.trackEvent("TierCRNFromDeadLetterQueue", mapOf("crn" to crn), null)
          eventAwsSqsClient.sendMessage(eventQueueUrl, msg.body)
          eventAwsSqsDlqClient.deleteMessage(DeleteMessageRequest(eventDlqUrl, msg.receiptHandle))
        }
    }
  }

  fun emptyMessages() {
    eventAwsSqsDlqClient.purgeQueue(PurgeQueueRequest(eventDlqUrl)).also { log.info("Emptying the dead letter queue") }
  }

  private fun getEventDlqMessageCount() =
    eventAwsSqsDlqClient.getQueueAttributes(eventDlqUrl, listOf("ApproximateNumberOfMessages"))
      .attributes["ApproximateNumberOfMessages"]
      ?.toInt() ?: 0

  data class SQSMessage(
    val Message: String,
    val MessageId: String
  )

  data class TierChangeEvent(
    val crn: String
  )
}
