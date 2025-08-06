package org.demoproject.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerListener implements ProducerListener<String, String> {

    @Override
    public void onSuccess(ProducerRecord<String, String> record, RecordMetadata metadata) {
        log.info("Kafka message sent successfully: topic={}, partition={}, offset={}, key={}, value={}",
                metadata.topic(), metadata.partition(), metadata.offset(),
                record.key(), record.value());
    }

    @Override
    public void onError(ProducerRecord<String, String> record, RecordMetadata metadata, Exception exception) {
        log.error("Kafka error sending message: topic={}, key={}, value={}, error={}",
                record.topic(), record.key(), record.value(), exception.getMessage(), exception);
    }
}

