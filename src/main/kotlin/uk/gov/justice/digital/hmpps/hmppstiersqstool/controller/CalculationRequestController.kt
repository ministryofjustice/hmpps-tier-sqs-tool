package uk.gov.justice.digital.hmpps.hmppstiersqstool.controller

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppstiersqstool.service.CalculationRequestService

@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class CalculationRequestController(private val calculationRequestService: CalculationRequestService) {

  @PutMapping("/file")
  fun uploadCsvFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
    calculationRequestService.uploadCsvFile(file)
    return ResponseEntity.ok().body("ok")
  }

  @PostMapping("/send")
  fun sendMessagesFromMessageBody(@RequestBody crns: Collection<String>): ResponseEntity<String> {
    calculationRequestService.sendMessagesFromList(crns)
    return ResponseEntity.ok().body("ok")
  }
}
