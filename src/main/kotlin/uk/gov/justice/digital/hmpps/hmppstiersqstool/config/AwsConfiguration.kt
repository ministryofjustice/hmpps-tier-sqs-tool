package uk.gov.justice.digital.hmpps.hmppstiersqstool.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.GetQueueUrlRequest
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
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

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

  }


  @Bean(name = ["eventAwsSqsClient"])
  fun eventAwsSQSAsync(): AmazonSQSAsync {
    val credentials: AWSCredentials = BasicAWSCredentials(mainAccessKeyId, mainSecretKey)
    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }

  @Primary
  @Bean(name = ["eventDlqAwsSqsClient"])
  fun eventDlqSQSAsync(): AmazonSQSAsync {
    log.info(region)
    println(dlqAccessKeyId)
    println(dlqSecretKey)

    val credentials: AWSCredentials = BasicAWSCredentials(dlqAccessKeyId, dlqSecretKey)

    return AmazonSQSAsyncClientBuilder
      .standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(credentials)).build()
  }

  @Bean
  fun mainMessageListenerContainerFactory(@Qualifier("eventDlqAwsSqsClient") eventDlqAwsSqsClient: AmazonSQSAsync, @Qualifier("eventAwsSqsClient") eventAwsSqsClient: AmazonSQSAsync, dynamicQueueUrlDestinationResolver: DynamicQueueUrlDestinationResolver): SimpleMessageListenerContainerFactory {
    return setupListenerContainerFactory(eventAwsSqsClient, dynamicQueueUrlDestinationResolver)
  }

  @Bean
  fun dynamicQueueUrlDestinationResolver(eventAwsSqsClient: AmazonSQSAsync, eventDlqAwsSqsClient: AmazonSQSAsync): DynamicQueueUrlDestinationResolver {
    log.info("creating destination resolver")
    return TwoQueuesDestinationResolver(eventAwsSqsClient, eventDlqAwsSqsClient, eventSQSQueueUrl)
  }

  private fun setupListenerContainerFactory(queue: AmazonSQSAsync, dynamicQueueUrlDestinationResolver: DynamicQueueUrlDestinationResolver): SimpleMessageListenerContainerFactory {
    val factory = SimpleMessageListenerContainerFactory()
    factory.setAmazonSqs(queue)
    factory.setDestinationResolver(dynamicQueueUrlDestinationResolver)
    factory.setMaxNumberOfMessages(10)
    factory.setWaitTimeOut(20)
    log.info("set up containerFactory for $queue")
    return factory
  }

  class TwoQueuesDestinationResolver(val eventAwsSQSAsync: AmazonSQSAsync, val eventDlqAwsSQSAsync: AmazonSQSAsync, val eventSQSQueueUrl: String) : DynamicQueueUrlDestinationResolver(eventAwsSQSAsync) {

    init {
      log.info("Destination resolver exists")
    }

    companion object {
      val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

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
      println("this is actually wotking")

      var queueName = name
      log.info("Resolving destination for $queueName")
      if (this.resourceIdResolver != null) {
        queueName = this.resourceIdResolver.resolveToPhysicalResourceId(name)
      }
      if (isValidQueueUrl(queueName)) {
        log.info("resolved to $queueName")

        return queueName
      }
      if (name.contains(eventSQSQueueUrl)) {
        try {
          val getQueueUrlResult = eventAwsSQSAsync.getQueueUrl(GetQueueUrlRequest(name))
          log.info("resolved to $getQueueUrlResult.queueUrl")

          return getQueueUrlResult.queueUrl
        } catch (e: QueueDoesNotExistException) {
          throw DestinationResolutionException(e.message, e)
        }
      } else {
        try {
          val getQueueUrlResult = this.eventDlqAwsSQSAsync.getQueueUrl(GetQueueUrlRequest(name))
          log.info("resolved to $getQueueUrlResult.queueUrl")

          return getQueueUrlResult.queueUrl
        } catch (e: QueueDoesNotExistException) {
          throw DestinationResolutionException(e.message, e)
        }
      }
    }
  }
}
