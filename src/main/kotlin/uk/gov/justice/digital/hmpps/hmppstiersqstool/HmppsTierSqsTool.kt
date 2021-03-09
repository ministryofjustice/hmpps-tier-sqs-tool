package uk.gov.justice.digital.hmpps.hmppstiersqstool

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsTierSqsTool

fun main(args: Array<String>) {
  runApplication<HmppsTierSqsTool>(*args)
}
