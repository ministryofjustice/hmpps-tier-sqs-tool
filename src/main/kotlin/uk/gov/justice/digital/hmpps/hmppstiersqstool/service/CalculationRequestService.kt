package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.SendMessageBatchRequest
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.entity.CalculationRequestEntity
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.repository.CalculationRequestRepository
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDateTime
import kotlin.streams.toList

@Service
class CalculationRequestService(
  private val calculationRequestRepository: CalculationRequestRepository,
  private val amazonSQS: AmazonSQSAsync,
  @Value("\${offender-events.sqs-queue}") val queueUrl: String
) {

  fun addToDatabase(crns: Collection<String>) {
    val now = LocalDateTime.now()
    crns
      .filter { crn -> calculationRequestRepository.findFirstByCrn(crn).also { log.info("Checking for Duplicate: $crn") } == null }
      .chunked(5000)
      .map {
        it.map { crn -> CalculationRequestEntity(crn = crn, created = now) }
      }
      .forEach {
        calculationRequestRepository.saveAll(it)
        log.info("Saved $it")
      }
  }

  fun sendMessagesFromDatabase() {
    do {
      val crnRequests = calculationRequestRepository.findAllByProcessedIsNull(PageRequest.of(0, PAGE_SIZE)).also {
        log.info("Fetched ${it.numberOfElements} calculation requests")
      }
      crnRequests.get().toList().map { it.crn }.chunked(6).forEach { crns ->
        sendMessagesFromList(crns).forEach { updateSentEntity(it.id, it.messageId) }
        Thread.sleep(1500L)
      }
    } while (crnRequests.totalPages > 1)
  }

  fun sendMessagesFromList(requests: Collection<String>): List<SendMessageBatchResultEntry> {
    val messageRequests = requests.map { SendMessageBatchRequestEntry(it, "{ \"Message\" : \"{\\\"crn\\\": \\\"$it\\\"}\" }") }
    log.info("Sending messages $requests")
    return amazonSQS.sendMessageBatch(SendMessageBatchRequest(queueUrl, messageRequests)).successful
  }

  fun uploadCsvFile(file: MultipartFile) {
    throwIfFileEmpty(file)
    var fileReader: BufferedReader? = null
    try {
      fileReader = BufferedReader(InputStreamReader(file.inputStream))
      addToDatabase(createCSVToBean(fileReader).parse().mapNotNull { it.crn })
    } catch (ex: Exception) {
      throw CsvImportException("Error during csv import")
    } finally {
      closeFileReader(fileReader)
    }
  }

  private fun throwIfFileEmpty(file: MultipartFile) {
    if (file.isEmpty)
      throw BadRequestException("Empty file")
  }

  private fun createCSVToBean(fileReader: BufferedReader?): CsvToBean<ActiveCrn> =
    CsvToBeanBuilder<ActiveCrn>(fileReader)
      .withType(ActiveCrn::class.java)
      .withSkipLines(1)
      .withIgnoreLeadingWhiteSpace(true)
      .build()

  private fun closeFileReader(fileReader: BufferedReader?) {
    try {
      fileReader!!.close()
    } catch (ex: IOException) {
      throw CsvImportException("Error during csv import")
    }
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

data class ActiveCrn(
  @CsvBindByPosition(position = 0)
  var crn: String? = null
)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(msg: String) : RuntimeException(msg)

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class CsvImportException(msg: String) : RuntimeException(msg)
