package ru.practicum.exception;

public class EventValidationException extends RuntimeException {
    public EventValidationException(String message) {
        super(message);
    }
}
