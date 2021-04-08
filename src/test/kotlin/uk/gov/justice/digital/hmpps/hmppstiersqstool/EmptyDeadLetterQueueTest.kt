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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmptyDeadLetterQueueTest {
// only to be run locally with localstack running
// if this fails, docker-compose up -d then retry
// circleCI will do this automatically in the pipeline

  @Autowired
  internal lateinit var eventAwsSqsDlqClient: AmazonSQSAsync

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Value("\${dlq.sqs-queue}")
  lateinit var eventDlqQueueUrl: String

  @BeforeEach
  fun `purge Queues`() {
    eventAwsSqsDlqClient.purgeQueue(PurgeQueueRequest(eventDlqQueueUrl))
  }

  @Test
  fun `removes all messages from DLQ`() {
    putMessageOnDlq(eventAwsSqsDlqClient, eventDlqQueueUrl)
    webTestClient.get().uri("/emptydlq")
      .exchange()
      .expectStatus()
      .isOk
    await untilCallTo { getNumberOfMessagesCurrentlyOnDeadLetterQueue(eventAwsSqsDlqClient, eventDlqQueueUrl) } matches { it == 0 }
  }
}
