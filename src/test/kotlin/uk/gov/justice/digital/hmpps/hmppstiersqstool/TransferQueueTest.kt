package uk.gov.justice.digital.hmpps.hmppstiersqstool

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.file.Files
import java.nio.file.Paths

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TransferQueueTest {
// only to be run locally with localstack running
// if this fails, docker-compose up -d then retry
// circleCI will do this automatically in the pipeline  

  @Autowired
  internal lateinit var eventAwsSqsClient: AmazonSQSAsync

  @Autowired
  internal lateinit var eventAwsSqsDlqClient: AmazonSQSAsync

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Value("\${offender-events-dlq.sqs-queue}")
  lateinit var eventDlqQueueUrl: String

  @Value("\${offender-events.sqs-queue}")
  lateinit var eventQueueUrl: String

  @BeforeEach
  fun `purge Queues`() {
    eventAwsSqsDlqClient.purgeQueue(PurgeQueueRequest(eventDlqQueueUrl))
    eventAwsSqsClient.purgeQueue(PurgeQueueRequest(eventQueueUrl))
  }

  @Test
  fun `moves message from DLQ to main queue`() {
    putMessageOnDlq()
    webTestClient.get().uri("/transfer")
      .exchange()
      .expectStatus()
      .isOk
    await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue() } matches { it == 0 }
    await untilCallTo { getNumberOfMessagesCurrentlyOnEventQueue() } matches { it == 1 }
  }

  private fun putMessageOnDlq() {
    val message = Files.readString(Paths.get("src/test/resources/fixtures/sqs/tier-calculation-event.json"))

    await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue() } matches { it == 0 }

    eventAwsSqsDlqClient.sendMessage(eventDlqQueueUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue() } matches { it == 1 }
  }

  fun getNumberOfMessagesCurrentlyOnDeadLetterQueue(): Int? {
    val queueAttributes = eventAwsSqsDlqClient.getQueueAttributes(eventDlqQueueUrl, listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }

  fun getNumberOfMessagesCurrentlyOnEventQueue(): Int? {
    val queueAttributes = eventAwsSqsClient.getQueueAttributes(eventQueueUrl, listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }
}
