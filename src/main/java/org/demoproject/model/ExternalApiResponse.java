package org.demoproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalApiResponse(
        @JsonProperty("eventId")
        Long eventId,

        @JsonProperty("currentScore")
        String currentScore
) {}
