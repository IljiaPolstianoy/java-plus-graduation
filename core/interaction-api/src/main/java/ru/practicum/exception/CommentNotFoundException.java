package ru.practicum.exception;

public class CommentNotFoundException extends Exception {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
