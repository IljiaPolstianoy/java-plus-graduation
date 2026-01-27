package ru.practicum.exception;

public class CategoryNameUniqueException extends Exception {
    public CategoryNameUniqueException(String message) {
        super(message);
    }
}
