package uk.gov.justice.digital.hmpps.hmppstiersqstool.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.entity.CalculationRequestEntity
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.repository.CalculationRequestRepository
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream


@Service
class CalculationRequestService(
  private val calculationRequestRepository: CalculationRequestRepository
  ) {

  fun processRequests() {
    val pageable: Pageable = PageRequest.of(0, PAGE_SIZE)
    val firstPage = getRequests(pageable)
    process(firstPage.get())

    var i = 1
    while(i < firstPage.totalPages) {
      process(getRequests(PageRequest.of(i, PAGE_SIZE)).get())
      i++
    }
  }

  fun addCrns(crns: Collection<String>) {
    crns.forEach { crn ->
      val request = calculationRequestRepository.findFirstByCrn(crn)
      if( request == null) {
        calculationRequestRepository.save(CalculationRequestEntity(crn = crn, created = LocalDateTime.now()))
        log.info("Saved $crn")
      } else {
        log.info("Already added $crn, skipping")
        log.info(request.toString())
      }
    }
  }

  fun processFromBody(crns: Collection<String>) {
    crns.forEach { crn ->
      sendSqsMessage(crn)
    }
  }

  private fun process(requests : Stream<CalculationRequestEntity>) {
    requests.forEach {
      if(it.messageId == null) {
        val messageId = sendSqsMessage(it.crn)
        updateCalculationRequest(it, messageId)
      } else {
        log.info("Already processed ${it.crn}, skipping")
        log.info(it.toString())
      }
    }
  }

  private fun sendSqsMessage(crn: String) : UUID {
    log.info("Sending SQS message for $crn")
    return UUID.randomUUID()
  }

  private fun updateCalculationRequest(calculationRequestEntity: CalculationRequestEntity, messageId : UUID) {
    calculationRequestEntity.let {
      it.processed = LocalDateTime.now()
      it.messageId = messageId
      calculationRequestRepository.save(it)
    }.also {
      log.info("Updated Calculation request ${it.crn} messageId ${it.messageId}")
      log.info(it.toString())
    }
  }

  private fun getRequests(pageable: Pageable) : Page<CalculationRequestEntity> {
    return calculationRequestRepository.findAll(pageable).also {
      log.info("Fetched ${it.numberOfElements} of limit ${pageable.pageSize} calculation requests on page ${it.number + 1} of ${it.totalPages}")
    }
  }


  companion object {
    private const val PAGE_SIZE = 1000
    private val log = LoggerFactory.getLogger(CalculationRequestService::class.java)
  }
}