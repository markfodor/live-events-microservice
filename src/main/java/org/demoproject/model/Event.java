package org.demoproject.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record Event(
        @NotNull(message = "Event ID is required")
        @Positive(message = "Event ID must be a positive number")
        Long eventId,

        @NotNull(message = "Event status is required")
        EventStatus status
) {
}
