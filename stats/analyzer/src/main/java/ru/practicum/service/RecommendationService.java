package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserEventInteraction;
import ru.practicum.storage.EventSimilarityRepository;
import ru.practicum.storage.UserEventInteractionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final EventSimilarityRepository similarityRepository;
    private final UserEventInteractionRepository interactionRepository;

    /**
     * Алгоритм 1: Поиск похожих мероприятий (GetSimilarEvents)
     */
    public List<RecommendedEvent> findSimilarEvents(
            final Integer eventId,
            final Integer userId,
            final Integer maxResults
    ) {
        log.info("Finding similar events for event={}, user={}, maxResults={}",
                eventId, userId, maxResults);

        // 1. Получить все похожие мероприятия
        final List<EventSimilarity> allSimilar = similarityRepository.findAllSimilarEvents(eventId);

        // 2. Исключить мероприятия, с которыми пользователь уже взаимодействовал
        final List<RecommendedEvent> filtered = allSimilar.stream()
                .map(sim -> {
                    final Integer otherEventId = sim.getEventAId().equals(eventId)
                            ? sim.getEventBId() : sim.getEventAId();
                    return new RecommendedEvent(otherEventId, sim.getScore());
                })
                .filter(rec -> !interactionRepository.existsByUserIdAndEventId(userId, rec.getEventId()))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(maxResults)
                .collect(Collectors.toList());

        log.info("Found {} similar events", filtered.size());
        return filtered;
    }

    /**
     * Алгоритм 2: Предсказание оценки для пользователя (GetRecommendationsForUser)
     */
    public List<RecommendedEvent> predictForUser(
            final Integer userId,
            final Integer maxResults
    ) {
        log.info("Predicting recommendations for user={}, maxResults={}", userId, maxResults);

        // 1. Получить N последних взаимодействий пользователя
        final int RECENT_INTERACTIONS_LIMIT = 10;
        final List<UserEventInteraction> recentInteractions =
                interactionRepository.findRecentByUserId(userId, RECENT_INTERACTIONS_LIMIT);

        if (recentInteractions.isEmpty()) {
            log.info("User {} has no interactions", userId);
            return Collections.emptyList();
        }

        // 2. Найти похожие мероприятия
        final Set<Integer> candidateEvents = new HashSet<>();
        final Map<Integer, Double> similarityScores = new HashMap<>();

        for (UserEventInteraction interaction : recentInteractions) {
            final List<EventSimilarity> similar = similarityRepository.findAllSimilarEvents(
                    interaction.getEventId());

            for (EventSimilarity sim : similar) {
                final Integer otherEventId = sim.getEventAId().equals(interaction.getEventId())
                        ? sim.getEventBId() : sim.getEventAId();

                // Исключаем уже просмотренные
                if (!interactionRepository.existsByUserIdAndEventId(userId, otherEventId)) {
                    candidateEvents.add(otherEventId);
                    // Сохраняем максимальное сходство
                    similarityScores.merge(otherEventId, sim.getScore(), Math::max);
                }
            }
        }

        // 3. Выбрать N самых похожих
        final List<Integer> topCandidates = candidateEvents.stream()
                .sorted((a, b) -> Double.compare(
                        similarityScores.getOrDefault(b, 0.0),
                        similarityScores.getOrDefault(a, 0.0)))
                .limit(maxResults)
                .toList();

        // 4. Предсказать оценку для каждого кандидата
        final List<RecommendedEvent> predictions = new ArrayList<>();
        for (Integer candidateId : topCandidates) {
            final double predictedScore = predictScore(userId, candidateId);
            predictions.add(new RecommendedEvent(candidateId, predictedScore));
        }

        // Сортируем по предсказанной оценке
        predictions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        log.info("Generated {} predictions for user {}", predictions.size(), userId);
        return predictions;
    }

    /**
     * Предсказание оценки для конкретного мероприятия на основе K ближайших соседей
     */
    private double predictScore(
            final Integer userId,
            final Integer targetEventId
    ) {
        final int K = 5;

        // 1. Найти K ближайших соседей (мероприятий, с которыми пользователь взаимодействовал)
        final List<UserEventInteraction> userInteractions =
                interactionRepository.findByUserIdOrderByTimestampDesc(userId);

        final List<EventSimilarity> neighbors = userInteractions.stream()
                .map(interaction -> similarityRepository
                        .findByEventAIdAndEventBId(
                                Math.min(interaction.getEventId(), targetEventId),
                                Math.max(interaction.getEventId(), targetEventId))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(K)
                .toList();

        if (neighbors.isEmpty()) {
            return 0.0;
        }

        // 2. Получить оценки пользователя для мероприятий-соседей
        final List<Integer> neighborEventIds = neighbors.stream()
                .map(sim -> sim.getEventAId().equals(targetEventId)
                        ? sim.getEventBId() : sim.getEventAId())
                .collect(Collectors.toList());

        final List<UserEventInteraction> neighborInteractions =
                interactionRepository.findByUserIdAndEventIdIn(userId, neighborEventIds);

        final Map<Integer, Double> eventScores = neighborInteractions.stream()
                .collect(Collectors.toMap(
                        UserEventInteraction::getEventId,
                        UserEventInteraction::getWeight
                ));

        // 3. Вычислить взвешенную сумму
        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (EventSimilarity neighbor : neighbors) {
            final Integer neighborEventId = neighbor.getEventAId().equals(targetEventId)
                    ? neighbor.getEventBId() : neighbor.getEventAId();

            final Double score = eventScores.get(neighborEventId);
            if (score != null) {
                weightedSum += neighbor.getScore() * score;
                similaritySum += neighbor.getScore();
            }
        }

        if (similaritySum == 0) {
            return 0.0;
        }

        return weightedSum / similaritySum;
    }

    /**
     * Алгоритм 3: Получение суммы максимальных весов взаимодействий
     * (для каждого пользователя берется только максимальный вес)
     */
    public List<RecommendedEvent> getInteractionsCount(final List<Integer> eventIds) {
        log.info("Getting interaction counts for {} events", eventIds.size());

        final List<RecommendedEvent> results = new ArrayList<>();

        for (Integer eventId : eventIds) {
            Double totalWeight = interactionRepository.sumMaxWeightsByEventId(eventId);

            if (totalWeight == null) {
                totalWeight = 0.0;
            }

            log.info("Event {}: total max weight sum = {}", eventId, totalWeight);
            results.add(new RecommendedEvent(eventId, totalWeight));
        }

        return results;
    }

    @lombok.Value
    public static class RecommendedEvent {
        Integer eventId;
        Double score;
    }
}