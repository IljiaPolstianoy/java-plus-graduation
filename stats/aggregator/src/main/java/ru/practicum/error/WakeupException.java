package ru.practicum.error;

public class WakeupException extends RuntimeException{
    public WakeupException(String message, Exception cause) {
        super(message, cause);
    }
}
