package uk.gov.justice.digital.hmpps.hmppstiersqstool

import com.amazonaws.services.sqs.AmazonSQSAsync

fun getNumberOfMessagesCurrentlyOnEventQueue(eventAwsSqsClient: AmazonSQSAsync, eventQueueUrl: String): Int? {
  val queueAttributes = eventAwsSqsClient.getQueueAttributes(eventQueueUrl, listOf("ApproximateNumberOfMessages"))
  return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
}
