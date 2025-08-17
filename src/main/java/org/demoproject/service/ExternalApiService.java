package org.demoproject.service;


import lombok.extern.slf4j.Slf4j;
import org.demoproject.exception.ExternalApiUnavailableException;
import org.demoproject.model.ExternalApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

@Slf4j
@Service
public class ExternalApiService {
    private final RestOperations restTemplate;
    private final String apiUrl;

    public ExternalApiService(RestOperations restTemplate,
                              @Value("${external-api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    // can add listener later to log the attempt numbers
    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    RestClientException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public ExternalApiResponse fetchEventScores(final Long eventId) {
        final String url = apiUrl + "/" + eventId;
        final ExternalApiResponse response = restTemplate.getForObject(url, ExternalApiResponse.class);

        if (response != null) {
            log.info("Successfully fetched data for event {}: {}", eventId, response);
            return response;
        } else {
            throw new RestClientException("Received null response for event: " + eventId);
        }
    }

    @Recover
    public ExternalApiResponse recover(final RestClientException exception, final Long eventId) {
        log.error("Failed to fetch event scores for eventID: {} after all retries. Reason: {}", eventId, exception.getMessage());
        throw new ExternalApiUnavailableException(
                "External API unavailable for event " + eventId + " after retrying", exception
        );
    }
}
