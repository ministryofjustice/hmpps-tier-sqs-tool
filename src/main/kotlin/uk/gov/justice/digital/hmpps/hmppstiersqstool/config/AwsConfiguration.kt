package uk.gov.justice.digital.hmpps.hmppstiersqstool.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.GetQueueUrlRequest
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.aws.core.env.ResourceIdResolver
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs
import org.springframework.cloud.aws.messaging.support.destination.DynamicQueueUrlDestinationResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.messaging.core.DestinationResolutionException
import java.net.URI
import java.net.URISyntaxException

@EnableSqs
@Configuration
@ConditionalOnProperty(name = ["offender-events.sqs-provider"], havingValue = "aws")
class AwsConfiguration(
  @Value("\${aws.offender-events-queue.access-key-id}") val mainAccessKeyId: String,
  @Value("\${aws.offender-events-queue.secret-access-key}") val mainSecretKey: String,
  @Value("\${aws.offender-events-dlq.access-key-id}") val dlqAccessKeyId: String,
  @Value("\${aws.offender-events-dlq.secret-access-key}") val dlqSecretKey: String,
  @Value("\${offender-events.sqs-queue}") val eventSQSQueueUrl: String,

  @Value("\${aws.region}") val region: String
) {

  @Primary
  @Bean(name = ["eventAwsSqsClient"])
  fun eventAwsSQSAsync(): AmazonSQSAsync {
    val credentials: AWSCredentials = BasicAWSCredentials(mainAccessKeyId, mainSecretKey)
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }

  @Bean(name = ["eventDlqAwsSqsClient"])
  fun eventDlqSQSAsync(): AmazonSQSAsync {
    val credentials: AWSCredentials = BasicAWSCredentials(dlqAccessKeyId, dlqSecretKey)
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }

  @Primary
  @Bean
  fun mainMessageListenerContainerFactory(eventAwsSqsClient: AmazonSQSAsync, dynamicQueueUrlDestinationResolver: DynamicQueueUrlDestinationResolver): SimpleMessageListenerContainerFactory {
    return setupListenerContainerFactory(eventAwsSqsClient, dynamicQueueUrlDestinationResolver)
  }

  @Bean
  fun dlqMessageListenerContainerFactory(eventDlqAwsSqsClient: AmazonSQSAsync, dynamicQueueUrlDestinationResolver: DynamicQueueUrlDestinationResolver): SimpleMessageListenerContainerFactory {
    return setupListenerContainerFactory(eventDlqAwsSqsClient, dynamicQueueUrlDestinationResolver)
  }

  @Bean
  fun dynamicQueueUrlDestinationResolver(eventAwsSqsClient: AmazonSQSAsync, eventDlqAwsSqsClient: AmazonSQSAsync): DynamicQueueUrlDestinationResolver {
    return TwoQueuesDestinationResolver(eventAwsSqsClient, eventDlqAwsSqsClient, eventSQSQueueUrl)
  }

  private fun setupListenerContainerFactory(queue: AmazonSQSAsync, dynamicQueueUrlDestinationResolver: DynamicQueueUrlDestinationResolver): SimpleMessageListenerContainerFactory {
    val factory = SimpleMessageListenerContainerFactory()
    factory.setAmazonSqs(queue)
    factory.setDestinationResolver(dynamicQueueUrlDestinationResolver)
    factory.setMaxNumberOfMessages(10)
    factory.setWaitTimeOut(20)
    return factory
  }

  class TwoQueuesDestinationResolver(val eventAwsSQSAsync: AmazonSQSAsync, val eventDlqAwsSQSAsync: AmazonSQSAsync, val eventSQSQueueUrl: String) : DynamicQueueUrlDestinationResolver(eventAwsSQSAsync) {

    private val resourceIdResolver: ResourceIdResolver? = null
    fun isValidQueueUrl(name: String): Boolean {
      try {
        var candidate = URI(name)
        return ("http".equals(candidate.getScheme()) || "https".equals(candidate.getScheme()))
      } catch (e: URISyntaxException) {
        return false
      }
    }

    @Throws(DestinationResolutionException::class)
    override fun resolveDestination(name: String): String? {
      var queueName = name
      if (this.resourceIdResolver != null) {
        queueName = this.resourceIdResolver.resolveToPhysicalResourceId(name)
      }
      if (isValidQueueUrl(queueName)) {
        return queueName
      }
      if (name.contains(eventSQSQueueUrl)) {
        try {
          val getQueueUrlResult = eventAwsSQSAsync.getQueueUrl(GetQueueUrlRequest(name))
          return getQueueUrlResult.queueUrl
        } catch (e: QueueDoesNotExistException) {
          throw DestinationResolutionException(e.message, e)
        }
      } else {
        try {
          val getQueueUrlResult = this.eventDlqAwsSQSAsync.getQueueUrl(GetQueueUrlRequest(name))
          return getQueueUrlResult.queueUrl
        } catch (e: QueueDoesNotExistException) {
          throw DestinationResolutionException(e.message, e)
        }
      }
    }
  }
}
