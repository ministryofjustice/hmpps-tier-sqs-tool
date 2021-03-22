package uk.gov.justice.digital.hmpps.hmppstiersqstool

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SendCrnsTest {

  @Autowired
  internal lateinit var eventAwsSqsClient: AmazonSQSAsync

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Value("\${offender-events.sqs-queue}")
  lateinit var eventQueueUrl: String

  @BeforeEach
  fun `purge Queues`() {
    eventAwsSqsClient.purgeQueue(PurgeQueueRequest(eventQueueUrl))
  }

  @Test
  fun `sends list of CRNs to queue`() {
    webTestClient
      .post()
      .uri("/send")
      .contentType(APPLICATION_JSON)
      .body(
        Mono.just("[\"CRN1\",\"CRN2\"]"), String::class.java
      )
      .exchange()
      .expectStatus()
      .isOk

    await untilCallTo { getNumberOfMessagesCurrentlyOnEventQueue(eventAwsSqsClient, eventQueueUrl) } matches { it == 2 }
    val message = eventAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventQueueUrl))
    assertThat(message.messages.get(0).body).contains("CRN1")
  }

  @Test
  fun `sends file of CRNs to queue`() {

    val multipartBodyBuilder = MultipartBodyBuilder()
    multipartBodyBuilder.part("file", ClassPathResource("fixtures/file/crns.csv"))
      .contentType(MediaType.MULTIPART_FORM_DATA)

    webTestClient.post()
      .uri("/file")
      .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
      .exchange()
      .expectStatus()
      .isOk

    await untilCallTo { getNumberOfMessagesCurrentlyOnEventQueue(eventAwsSqsClient, eventQueueUrl) } matches { it == 3 }
    val message = eventAwsSqsClient.receiveMessage(ReceiveMessageRequest(eventQueueUrl))
    assertThat(message.messages.get(0).body).contains("CRNFromFile")
  }
}
