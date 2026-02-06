package ru.practicum.exception;

public class LocationNotFound extends RuntimeException {
    public LocationNotFound(String message) {
        super(message);
    }
}
