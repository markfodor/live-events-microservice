package org.demoproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventApiResponse(
        @JsonProperty("eventId")
        String eventId,

        @JsonProperty("currentScore")
        String currentScore
) {}
