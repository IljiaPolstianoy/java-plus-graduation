package ru.practicum.exception;

public class EventAlreadyPublishedException extends Exception {
    public EventAlreadyPublishedException(String message) {
        super(message);
    }
}
