package ru.practicum.exception;

public class RequestSelfAttendException extends RuntimeException {
    public RequestSelfAttendException(String message) {
        super(message);
    }
}
