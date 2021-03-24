package uk.gov.justice.digital.hmpps.hmppstiersqstool.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppstiersqstool.service.QueueAdminService

@RestController
class QueueAdminController(private val queueAdminService: QueueAdminService) {

  @GetMapping("/transfer")
  fun transferMessagesFromDeadLetterQueue() {
    queueAdminService.transferMessages()
  }

  @GetMapping("/emptydlq")
  fun emptyMessagesFromDeadLetterQueue() {
    queueAdminService.emptyMessages()
  }
}
