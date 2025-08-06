package org.demoproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;


@Slf4j
@Service
public class ScorePublisherService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    public ScorePublisherService(KafkaTemplate<String, String> kafkaTemplate,
                                 @Value("${app.kafka.topic:sports-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publishEventResult(Long eventId, String result) {
        log.debug("Attempting to publish message for event: {}", eventId);

        kafkaTemplate.send(
                topicName, String.valueOf(eventId), result
        );
    }
}