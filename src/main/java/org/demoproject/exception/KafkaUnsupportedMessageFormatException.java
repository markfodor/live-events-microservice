package org.demoproject.exception;

public class KafkaUnsupportedMessageFormatException  extends RuntimeException {
    public KafkaUnsupportedMessageFormatException(String message) {
        super(message);
    }

    public KafkaUnsupportedMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
