package com.pedidos360_report.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

  private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(
      KafkaTemplate<String, String> kafkaTemplate) {

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,

        (consumerRecord, exception) -> {

          log.error(
              "Evento Kafka fallido: topic={}, partition={}, offset={}",
              consumerRecord.topic(),
              consumerRecord.partition(),
              consumerRecord.offset(),
              exception);

          return new TopicPartition(
              consumerRecord.topic() + ".DLT",
              consumerRecord.partition());
        });

    recoverer.setFailIfSendResultIsError(true);

    DefaultErrorHandler handler = new DefaultErrorHandler(
        recoverer,
        new FixedBackOff(1000L, 2L));

    handler.addNotRetryableExceptions(
        IllegalArgumentException.class);

    return handler;
  }

  @Bean
  public NewTopic estadosDlt(
      @Value("${app.kafka.topics.estados}") String topic) {

    return TopicBuilder.name(topic + ".DLT")
        .partitions(3)
        .replicas(2)
        .build();
  }

  @Bean
  public NewTopic ventasDlt(
      @Value("${app.kafka.topics.ventas}") String topic) {

    return TopicBuilder.name(topic + ".DLT")
        .partitions(3)
        .replicas(2)
        .build();
  }
}
