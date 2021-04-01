package uk.gov.justice.digital.hmpps.hmppstiersqstool.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class TelemetryConfig {

  @Bean
  fun getTelemetryClient(): TelemetryClient {
    return TelemetryClient()
  }
}
