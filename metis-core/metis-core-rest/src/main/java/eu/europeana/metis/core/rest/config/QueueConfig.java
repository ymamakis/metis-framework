package eu.europeana.metis.core.rest.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.ForgivingExceptionHandler;
import eu.europeana.metis.core.execution.QueueConsumer;
import eu.europeana.metis.core.execution.WorkflowExecutionMonitor;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@EnableScheduling
public class QueueConfig implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueConfig.class);

  private final ConfigurationPropertiesHolder propertiesHolder;
  private QueueConsumer queueConsumer;

  private Connection connection;
  private Channel publisherChannel;
  private Channel consumerChannel;

  /**
   * Constructor with the required properties class.
   *
   * @param propertiesHolder the properties holder
   */
  @Autowired
  public QueueConfig(ConfigurationPropertiesHolder propertiesHolder) {
    this.propertiesHolder = propertiesHolder;
  }

  @Bean
  Connection getConnection()
      throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(propertiesHolder.getRabbitmqHost());
    connectionFactory.setPort(propertiesHolder.getRabbitmqPort());
    connectionFactory.setVirtualHost(
        StringUtils.isNotBlank(propertiesHolder.getRabbitmqVirtualHost()) ? propertiesHolder
            .getRabbitmqVirtualHost() : "/");
    connectionFactory.setUsername(propertiesHolder.getRabbitmqUsername());
    connectionFactory.setPassword(propertiesHolder.getRabbitmqPassword());
    connectionFactory.setAutomaticRecoveryEnabled(true);
    if (propertiesHolder.isRabbitmqEnableSSL()) {
      connectionFactory.useSslProtocol();
    }
    //Does not close the channel if an unhandled exception occurred
    //Can happen in QueueConsumer and it's safe to not handle the execution, it will be picked up
    //again from the failsafe Executor.
    connectionFactory.setExceptionHandler(new ForgivingExceptionHandler());
    connection = connectionFactory.newConnection();
    return connection;
  }

  @Bean(name = "rabbitmqPublisherChannel")
  Channel getRabbitmqPublisherChannel(Connection connection) throws IOException {
    publisherChannel = connection.createChannel();
    setupChannelProperties(publisherChannel);
    return publisherChannel;
  }

  @Bean(name = "rabbitmqConsumerChannel")
  Channel getRabbitmqConsumerChannel(Connection connection) throws IOException {
    consumerChannel = connection.createChannel();
    setupChannelProperties(consumerChannel);
    return consumerChannel;
  }

  private void setupChannelProperties(Channel channel) throws IOException {
    Map<String, Object> args = new ConcurrentHashMap<>();
    args.put("x-max-priority",
        propertiesHolder.getRabbitmqHighestPriority());//Higher number means higher priority
    //Second boolean durable to false
    channel.queueDeclare(propertiesHolder.getRabbitmqQueueName(), false, false, false, args);
  }

  @Bean
  public QueueConsumer getQueueConsumer(WorkflowExecutorManager workflowExecutionManager,
      WorkflowExecutionMonitor workflowExecutionMonitor,
      @Qualifier("rabbitmqConsumerChannel") Channel rabbitmqConsumerChannel) throws IOException {
    queueConsumer = new QueueConsumer(rabbitmqConsumerChannel,
        propertiesHolder.getRabbitmqQueueName(), workflowExecutionManager, workflowExecutionManager,
        workflowExecutionMonitor);
    return queueConsumer;
  }

  @Scheduled(fixedDelayString = "${polling.timeout.for.cleaning.completion.service.in.millisecs}", initialDelayString = "${polling.timeout.for.cleaning.completion.service.in.millisecs}")
  public void runQueueConsumerCleanup() throws InterruptedException {
    LOGGER.debug("Queue consumer cleanup started (runs every {} milliseconds).",
        propertiesHolder.getPollingTimeoutForCleaningCompletionServiceInMillisecs());
    this.queueConsumer.checkAndCleanCompletionService();
    LOGGER.debug("Queue consumer cleanup finished.");
  }

  /**
   * Close resources.
   *
   * @throws GenericMetisException if a resource failed to close
   */
  @PreDestroy
  public void close() throws GenericMetisException {
    try {
      // Shut down RabbitMQ
      if (publisherChannel != null && publisherChannel.isOpen()) {
        publisherChannel.close();
      }
      if (consumerChannel != null && consumerChannel.isOpen()) {
        consumerChannel.close();
      }
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
      // Shutdown the queue consumer
      if (queueConsumer != null) {
        queueConsumer.close();
      }
    } catch (IOException | TimeoutException e) {
      throw new GenericMetisException("Could not shutdown resources properly.", e);
    }
  }

}
