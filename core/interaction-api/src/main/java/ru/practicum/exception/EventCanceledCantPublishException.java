package ru.practicum.exception;

public class EventCanceledCantPublishException extends RuntimeException {
    public EventCanceledCantPublishException(String message) {
        super(message);
    }
}
