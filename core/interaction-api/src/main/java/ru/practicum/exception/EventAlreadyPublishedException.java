package ru.practicum.exception;

public class EventAlreadyPublishedException extends RuntimeException {
    public EventAlreadyPublishedException(String message) {
        super(message);
    }
}
