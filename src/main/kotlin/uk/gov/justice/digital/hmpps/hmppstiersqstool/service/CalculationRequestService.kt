package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.SendMessageBatchRequest
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry
import kotlin.streams.toList
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.entity.CalculationRequestEntity
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.repository.CalculationRequestRepository
import java.time.LocalDateTime

@Service
class CalculationRequestService(
  private val calculationRequestRepository: CalculationRequestRepository,
  private val amazonSQS: AmazonSQSAsync,
  @Value("\${offender-events.sqs-queue}") val queueUrl: String
) {

  fun addToDatabase(crns: Collection<String>) =
    crns.forEach { crn ->
      calculationRequestRepository.findFirstByCrn(crn).let {
        if (it == null) {
          calculationRequestRepository.save(CalculationRequestEntity(crn = crn, created = LocalDateTime.now()))
          log.info("Saved $crn")
        } else {
          log.info("Already added $crn, skipping")
          log.info(it.toString())
        }
      }
    }

  fun sendMessagesFromDatabase() {
    do {
      val crnRequests = calculationRequestRepository.findAllByProcessedIsNull(PageRequest.of(0, PAGE_SIZE)).also {
        log.info("Fetched ${it.numberOfElements} calculation requests")
      }
      sendMessagesFromList(crnRequests.get().toList().map { it.crn }).forEach { updateSentEntity(it.id, it.messageId) }
    } while (crnRequests.totalPages > 1)
  }

  fun sendMessagesFromList(requests: Collection<String>) : List<SendMessageBatchResultEntry> =
    requests.chunked(10)
      .flatMap { messages ->
      val messageRequests = messages.map { SendMessageBatchRequestEntry(it, "{ \"Message\" : \"{\\\"crn\\\": \\\"$it\\\"}\" }") }
      amazonSQS.sendMessageBatch(SendMessageBatchRequest(queueUrl, messageRequests)).successful
    }

  private fun updateSentEntity(crn: String, messageId: String) =
    calculationRequestRepository.findFirstByCrn(crn)?.let {
      it.processed = LocalDateTime.now()
      it.messageId = messageId
      calculationRequestRepository.save(it)
    }.also {
      log.info("Updated Calculation request ${it?.crn}")
      log.info(it.toString())
    }

  companion object {
    private const val PAGE_SIZE = 1000
    private val log = LoggerFactory.getLogger(CalculationRequestService::class.java)
  }
}
