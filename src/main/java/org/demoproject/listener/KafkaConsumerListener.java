package org.demoproject.listener;

import lombok.extern.slf4j.Slf4j;
import org.demoproject.configuration.KafkaProperties;
import org.demoproject.exception.DeadLetterMessageHandlingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SportsEventsListener {

    private final KafkaProperties kafkaProperties;

    public SportsEventsListener(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @KafkaListener(
            topics = "sports-events",
            groupId = "#{kafkaProperties.getConsumer().getGroupId()}"
    )
    public void consumeSportsEvents(String message) {
        log.info("Received sports event message: {}", message);
    }

    @KafkaListener(
            topics = "sports-events-dlt",
            groupId = "#{kafkaProperties.getConsumer().getDltGroupId()}"
    )
    public void handleDeadLetter(String failedMessage) {
        throw new DeadLetterMessageHandlingException("Permanent failure for message: " + failedMessage);
    }
}
