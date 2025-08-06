package org.demoproject.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"sports-events", "sports-events-dlt"},
        bootstrapServersProperty = "app.kafka.bootstrap-servers"
)
class KafkaMessagingIntegrationTest {
    public static final String SPORTS_EVENTS_TOPIC = "sports-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        var consumerProps = KafkaTestUtils.consumerProps(
                "test-group",
                "true",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of(SPORTS_EVENTS_TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void testMessageProcessingFailure() {
        String failingMessage = "asd".repeat(1000000);
        SendResult<String, String> result = null;

        try {
            result = kafkaTemplate.send(SPORTS_EVENTS_TOPIC, failingMessage).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RecordTooLargeException.class);
        }

        assertThat(result).isNull();
    }

    @Test
    void testSuccessfulMessage() throws Exception {
        var successMessage = "1:1";

        kafkaTemplate.send(SPORTS_EVENTS_TOPIC, successMessage).get(5, TimeUnit.SECONDS);

        var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
        assertThat(records).isNotEmpty();
    }

    @Test
    void testTopicCorrectness() throws Exception {
        String message = "1:1";
        String fakeTopic = "fake_topic";

        kafkaTemplate.send(fakeTopic, message).get(5, TimeUnit.SECONDS);

        var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
        assertThat(records).isEmpty();
    }
}

