package org.demoproject.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerListener {

    @KafkaListener(
            topics = "#{kafkaProperties.getTopic()}",
            groupId = "#{kafkaProperties.getProducer().getGroupId()}"
    )
    public void consumeSportsEvents(String message) {
        log.info("Kafka received sports event message: {}", message);
    }
}
