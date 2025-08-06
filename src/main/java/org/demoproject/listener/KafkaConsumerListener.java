package org.demoproject.listener;

import lombok.extern.slf4j.Slf4j;
import org.demoproject.configuration.KafkaProperties;
import org.demoproject.exception.DeadLetterMessageHandlingException;
import org.demoproject.exception.KafkaUnsupportedMessageFormatException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerListener {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerListener(final KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // TODO properties!
    @KafkaListener(
            topics = "#{kafkaProperties.getTopic()}",
            groupId = "#{kafkaProperties.getConsumer().getGroupId()}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSportsEvents(String message) {
        log.info("Kafka received sports event message: {}", message);

        // TODO test - it works on the consumer side
//        if (message.equals("")) {
//            throw new KafkaUnsupportedMessageFormatException(message);
//        }
    }

    // TODO remove
    @KafkaListener(
            topics = "#{kafkaProperties.getTopic()}",
            groupId = "#{kafkaProperties.getConsumer().getDltGroupId()}"
//            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeadLetter(String failedMessage) {
        throw new DeadLetterMessageHandlingException("Kafka permanent failure for message: " + failedMessage);
    }

    @DltHandler
    public void handleDeadLetterMesssage(String failedMessage) {
        log.error("Kafka failed event message: {}", failedMessage);
    }
}
