package ru.practicum.exception;

public class ParticipantLimitExceededException extends RuntimeException {
    public ParticipantLimitExceededException(String message) {
        super(message);
    }
}
