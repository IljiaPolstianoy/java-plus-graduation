package ru.practicum.model;

import lombok.Data;

@Data
public class RecommendationResult {
    private final Integer eventId;
    private final Double score;
}