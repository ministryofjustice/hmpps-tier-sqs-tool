package uk.gov.justice.digital.hmpps.hmppstiersqstool.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["sqs-provider"], havingValue = "aws")
class AwsConfiguration(
  @Value("\${offender-events.access-key-id}") val eventAccessKeyId: String,
  @Value("\${offender-events.secret-access-key}") val eventSecretKey: String,
  @Value("\${offender-events.region}") val eventRegion: String,
  @Value("\${offender-events-dlq.access-key-id}") val dlqAccessKeyId: String,
  @Value("\${offender-events-dlq.secret-access-key}") val dlqSecretKey: String,
  @Value("\${offender-events-dlq.region}") val dlqRegion: String
) {

  @Bean(name = ["eventAwsSqsClient"])
  fun eventAwsSqsClient(): AmazonSQSAsync {
    val credentials: AWSCredentials = BasicAWSCredentials(eventAccessKeyId, eventSecretKey)
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(eventRegion)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }

  @Bean(name = ["eventAwsSqsDlqClient"])
  fun eventAwsSqsDlqClient(): AmazonSQSAsync {
    val credentials: AWSCredentials = BasicAWSCredentials(dlqAccessKeyId, dlqSecretKey)
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(dlqRegion)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }
}
