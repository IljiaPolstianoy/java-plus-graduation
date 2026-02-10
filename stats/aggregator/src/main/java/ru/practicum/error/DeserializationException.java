package ru.practicum.error;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Exception cause) {
        super(message, cause);
    }
}
