package org.demoproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MessagePublishingService {
    // TODO auto format files
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    public MessagePublishingService(KafkaTemplate<String, String> kafkaTemplate,
                                    @Value("${app.kafka.topic:sports-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publishEventResult(Long eventId, String result) {
        log.debug("Publishing message for event: {}", eventId);

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topicName, String.valueOf(eventId), result);

        future.whenComplete((r, exception) -> {
            if (exception == null) {
                log.info("Successfully published message for event {} to topic {} at offset {}",
                        r, topicName, r.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish message for event {} to topic {}: {}",
                        r, topicName, exception.getMessage());

                // TODO in production env, you need to send to a dead letter queue here
            }
        });
    }
}