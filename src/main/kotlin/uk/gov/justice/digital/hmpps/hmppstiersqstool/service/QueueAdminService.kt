package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QueueAdminService(

  private val eventAwsSqsClient: AmazonSQS,
  private val eventDlqAwsSqsClient: AmazonSQS,
  @Value("\${offender-events.sqs-queue}") private val eventQueueUrl: String,
  @Value("\${offender-events.sqs-dlq}") private val eventDlqName: String,
) {

//  init {
//    log.info(eventDlqName)
//
//    log.info(eventDlqAwsSqsClient.getQueueUrl(eventDlqName).queueUrl)
//  }

  // tried passing in the name and the URL
  // name gives a bad request - queue does not exist or you do not have access to it
  // url gives 403 forbidden
//  val eventDlqUrl: String by lazy { eventDlqAwsSqsClient.getQueueUrl(eventDlqName).queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferEventMessages() =
    repeat(1) {



      var messageFromDlq = eventDlqAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventDlqName).withMaxNumberOfMessages(1))
      log.info("Received message from DLQ $messageFromDlq")
      var dlqResult = eventDlqAwsSqsClient.sendMessage(SendMessageRequest(eventDlqName, "{ \"Message\" : \"{\\\"crn\\\": \\\"X386896\\\"}\" }"))
      log.info("result of send to dlq client $dlqResult")

      var qResult = eventAwsSqsClient.sendMessage(SendMessageRequest(eventQueueUrl, "{ \"Message\" : \"{\\\"crn\\\": \\\"X386897\\\"}\" }")) // worked!
      log.info("result of send to main client $qResult")
      var messageFromMain = eventAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventQueueUrl).withMaxNumberOfMessages(1))
      log.info("Received message from main queue $messageFromMain")

      eventDlqAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventDlqName).withMaxNumberOfMessages(1)).messages
        .forEach { msg ->
          log.info("Got a message: $msg.body")
//          eventAwsSqsClient.sendMessage(eventQueueUrl, msg.body)
//          eventDlqAwsSqsClient.deleteMessage(DeleteMessageRequest(eventDlqUrl, msg.receiptHandle))
          log.info("Moved a message from the dlq to the main queue: $msg.receiptHandle")
        }
    }

  private fun getEventDlqMessageCount() =
    eventDlqAwsSqsClient.getQueueAttributes(eventDlqName, listOf("ApproximateNumberOfMessages"))
      .attributes["ApproximateNumberOfMessages"]
      ?.toInt() ?: 0
}
