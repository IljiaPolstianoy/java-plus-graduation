package ru.practicum.exception;

public class FilterValidationException extends RuntimeException {
    public FilterValidationException(String message) {
        super(message);
    }
}
