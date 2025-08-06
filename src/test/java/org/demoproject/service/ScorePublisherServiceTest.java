package org.demoproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.CompletableFuture;

// OK
class ScorePublisherServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ScorePublisherService scorePublisherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scorePublisherService = new ScorePublisherService(kafkaTemplate, "test-topic");
    }

    @Test
    void testPublishMessageSuccessfully() {
        Long eventId = 42L;
        String result = "2:1";

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        scorePublisherService.publishEventResult(eventId, result);

        verify(kafkaTemplate).send("test-topic", "42", result);
    }

    @Test
    void testKafkaSendFailure() {
        Long eventId = 42L;
        String result = "2:1";

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Simulated failure"));

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failedFuture);

        scorePublisherService.publishEventResult(eventId, result);

        verify(kafkaTemplate).send("test-topic", "42", result);
    }
}
