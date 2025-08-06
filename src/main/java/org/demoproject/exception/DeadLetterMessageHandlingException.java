package org.demoproject.exception;

public class DeadLetterMessageHandlingException extends RuntimeException {
    public DeadLetterMessageHandlingException(String message) {
        super(message);
    }

    public DeadLetterMessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
