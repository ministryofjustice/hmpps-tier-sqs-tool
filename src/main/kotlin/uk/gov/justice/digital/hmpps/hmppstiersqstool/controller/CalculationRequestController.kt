package uk.gov.justice.digital.hmpps.hmppstiersqstool.controller

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.hmppstiersqstool.service.CalculationRequestService

@RestController
@RequestMapping(produces = [APPLICATION_JSON_VALUE])
class CalculationRequestController(private val calculationRequestService: CalculationRequestService) {


  @GetMapping("/database/trigger")
  fun getLatestTierCalculation(): ResponseEntity<String> {
    calculationRequestService.processRequests()
    return ResponseEntity.ok().body("ok")
  }

  @PutMapping("/database/add")
  fun addCRNs(@RequestBody crns : Collection<String>): ResponseEntity<String> {
    calculationRequestService.addCrns(crns)
    return ResponseEntity.ok().body("ok")
  }

  @PostMapping("/body/trigger")
  fun addCRNsAndTrigger(@RequestBody crns : Collection<String>): ResponseEntity<String> {
    calculationRequestService.processFromBody(crns)
    return ResponseEntity.ok().body("ok")
  }
}
