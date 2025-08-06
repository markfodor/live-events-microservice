package org.demoproject.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// TODO delete
@Slf4j
//@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

//    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, this::createDltTopicPartition);
        var backOff = new FixedBackOff(
                kafkaProperties.getConsumer().getRetryBackoffMs(),
                kafkaProperties.getConsumer().getMaxRetries()
        );

        var errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addRetryableExceptions(KafkaException.class);
        errorHandler.setRetryListeners(this::logRetryAttempt);

        return errorHandler;
    }

    private TopicPartition createDltTopicPartition(ConsumerRecord<?, ?> record, Exception ex) {
        var dltTopic = record.topic() + "-dlt";
        log.warn("Kafka Sending failed message to DLT: {}", dltTopic);
        return new TopicPartition(dltTopic, record.partition());
    }

    private void logRetryAttempt(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
        var retryInfo = new RetryLogData(deliveryAttempt, record.topic(), ex.getMessage());
        log.warn("Kafka Retry attempt {} for record on topic {}: {}",
                retryInfo.attempt(), retryInfo.topic(), retryInfo.error());
    }

    private record RetryLogData(int attempt, String topic, String error) {}
}
