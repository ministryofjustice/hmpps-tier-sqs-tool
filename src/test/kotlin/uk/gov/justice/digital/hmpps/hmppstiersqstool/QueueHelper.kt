package uk.gov.justice.digital.hmpps.hmppstiersqstool

import com.amazonaws.services.sqs.AmazonSQSAsync
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import java.nio.file.Files
import java.nio.file.Paths

fun getNumberOfMessagesCurrentlyOnEventQueue(eventAwsSqsClient: AmazonSQSAsync, eventQueueUrl: String): Int? {
  val queueAttributes = eventAwsSqsClient.getQueueAttributes(eventQueueUrl, listOf("ApproximateNumberOfMessages"))
  return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
}

fun putMessageOnDlq(eventAwsSqsDlqClient: AmazonSQSAsync, eventDlqQueueUrl: String) {
  val message = Files.readString(Paths.get("src/test/resources/fixtures/sqs/tier-calculation-event.json"))

  await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue(eventAwsSqsDlqClient, eventDlqQueueUrl) } matches { it == 0 }

  eventAwsSqsDlqClient.sendMessage(eventDlqQueueUrl, message)

  await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue(eventAwsSqsDlqClient, eventDlqQueueUrl) } matches { it == 1 }
}

fun getNumberOfMessagesCurrentlyOnDeadLetterQueue(eventAwsSqsDlqClient: AmazonSQSAsync, eventDlqQueueUrl: String): Int? {
  val queueAttributes = eventAwsSqsDlqClient.getQueueAttributes(eventDlqQueueUrl, listOf("ApproximateNumberOfMessages"))
  return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
}
