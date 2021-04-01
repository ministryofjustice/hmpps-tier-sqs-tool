package uk.gov.justice.digital.hmpps.hmppstiersqstool

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TelemetryTestConfig {

  @MockBean
  var telemetryClient: TelemetryClient? = null
}
