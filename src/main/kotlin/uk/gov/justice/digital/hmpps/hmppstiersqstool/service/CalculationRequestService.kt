package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.SendMessageBatchRequest
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Service
class CalculationRequestService(
  private val eventAwsSqsClient: AmazonSQSAsync,
  @Value("\${offender-events.sqs-queue}") val queueUrl: String
) {

  fun sendMessagesFromList(requests: Collection<String>) {
    requests
      .map { SendMessageBatchRequestEntry(it, "{ \"Message\" : \"{\\\"crn\\\": \\\"$it\\\"}\" }") }
      .chunked(10).forEach { messageRequests ->
        messageRequests.forEach { req ->
          log.info("Sending message, ${req.messageBody}")
        }
        eventAwsSqsClient.sendMessageBatch(SendMessageBatchRequest(queueUrl, messageRequests))
        log.info("Sent Batch")
        // Thread.sleep(1000L)
      }
  }

  fun uploadCsvFile(file: MultipartFile) {
    throwIfFileEmpty(file)
    var fileReader: BufferedReader? = null
    try {
      fileReader = BufferedReader(InputStreamReader(file.inputStream))
      log.info("Starting converting from CSV")
      val requests = createCSVToBean(fileReader).parse().mapNotNull { it.crn }
      log.info("Finished converting from CSV ${requests.size} entries")
      log.info("Starting sending messages")
      sendMessagesFromList(requests)
      log.info("Finished sending messages")
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

  companion object {
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
