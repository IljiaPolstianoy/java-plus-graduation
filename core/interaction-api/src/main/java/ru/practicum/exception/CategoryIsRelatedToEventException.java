package ru.practicum.exception;

public class CategoryIsRelatedToEventException extends RuntimeException {
    public CategoryIsRelatedToEventException(String message) {
        super(message);
    }
}
