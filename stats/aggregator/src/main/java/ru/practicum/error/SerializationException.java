package ru.practicum.error;

public class SerializationException extends RuntimeException {
    public SerializationException(String message, Exception cause) {
        super(message, cause);
    }
}
