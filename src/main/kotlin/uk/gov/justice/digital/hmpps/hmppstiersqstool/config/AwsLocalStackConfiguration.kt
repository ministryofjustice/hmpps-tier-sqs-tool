package uk.gov.justice.digital.hmpps.hmppstiersqstool.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["sqs-provider"], havingValue = "localstack")
class AwsLocalStackConfiguration(
  @Value("\${main-queue.region}") val eventEndpoint: String,
  @Value("\${main-queue.endpoint}") val eventRegion: String,
  @Value("\${dlq.region}") val dlqEndpoint: String,
  @Value("\${dlq.endpoint}") val dlqRegion: String
) {

  @Bean(name = ["eventAwsSqsClient"])
  fun eventAwsSqsClient(): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(eventEndpoint, eventRegion))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()
  }

  @Bean(name = ["eventAwsSqsDlqClient"])
  fun eventAwsSqsDlqClient(): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dlqEndpoint, dlqRegion))
      .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
      .build()
  }
}
