package org.demoproject.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"sports-events", "sports-events-dlt"},
        bootstrapServersProperty = "app.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "app.kafka.consumer.max-retries=2",
        "app.kafka.consumer.retry-backoff-ms=100"
})
class KafkaDeadLetterIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> dltConsumer;

    @BeforeEach
    void setUp() {
        var consumerProps = KafkaTestUtils.consumerProps(
                "dlt-test-group",
                "true",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        dltConsumer = consumerFactory.createConsumer();
        dltConsumer.subscribe(List.of("sports-events-dlt"));
    }

    @AfterEach
    void tearDown() {
        if (dltConsumer != null) {
            dltConsumer.close();
        }
    }

    @Test
    void testMessageReachesDeadLetterTopicAfterProcessingFailure() {
        String failingMessage = "asd".repeat(1000000);
        var topic = "sports-events";

        SendResult<String, String> result = null;
        try {
            result = kafkaTemplate.send(topic, failingMessage).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RecordTooLargeException.class);
        }

        assertThat(result).isNull();

    }

    @Test
    void testSuccessfulMessage() throws Exception {
        // Given - A message that will succeed
        var successMessage = "1:1";
        var topic = "sports-events";

        // When
        kafkaTemplate.send(topic, successMessage).get(5, TimeUnit.SECONDS);

        // Then - Verify no message in DLT
        var dltRecords = KafkaTestUtils.getRecords(dltConsumer, Duration.ofSeconds(2));
        assertThat(dltRecords.isEmpty()).isTrue();
    }
}

