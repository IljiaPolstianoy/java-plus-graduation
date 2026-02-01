package ru.practicum.stats.error;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message, Exception cause) {
        super(message, cause);
    }
}
