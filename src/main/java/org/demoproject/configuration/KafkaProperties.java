package org.demoproject.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {
    // with default values, just to make sure
    private String bootstrapServers = "localhost:9092";
    private String topic;
    private Producer producer = new Producer();

    @Data
    public static class Producer {
        private String acks = "1";
        private int retries = 3;
        private long retryBackoffMs = 1000L;
        private String groupId = "sports-events-consumer-group";
    }
}


