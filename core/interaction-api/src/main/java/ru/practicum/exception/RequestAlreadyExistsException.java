package ru.practicum.exception;

public class RequestAlreadyExistsException extends Exception {
    public RequestAlreadyExistsException(String message) {
        super(message);
    }
}
