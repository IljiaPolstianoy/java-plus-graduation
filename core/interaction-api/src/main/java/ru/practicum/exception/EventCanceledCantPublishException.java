package ru.practicum.exception;

public class EventCanceledCantPublishException extends Exception {
    public EventCanceledCantPublishException(String message) {
        super(message);
    }
}
