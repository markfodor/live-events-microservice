package org.demoproject.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.demoproject.listener.KafkaProducerListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    private final KafkaProducerListener kafkaProducerListener;

    public KafkaProducerConfig(final KafkaProperties kafkaProperties,
                               final KafkaProducerListener kafkaProducerListener) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaProducerListener = kafkaProducerListener;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        var configProps = Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks(),
                ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries(),
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaProperties.getProducer().getRetryBackoffMs()
        );

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        return kafkaTemplate;
    }
}

