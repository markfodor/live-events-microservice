package org.demoproject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventStatus {
    LIVE("live"),
    NOT_LIVE("not live");

    private final String text;

    EventStatus(final String text) {
        this.text = text;
    }

    @JsonValue
    public String getText() {
        return text;
    }

    @JsonCreator
    public static EventStatus forValue(final String value) {
        for (EventStatus status : EventStatus.values()) {
            if (status.text.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
