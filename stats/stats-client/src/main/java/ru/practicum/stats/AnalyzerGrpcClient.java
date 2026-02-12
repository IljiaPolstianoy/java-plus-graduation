package ru.practicum.stats;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    /**
     * Получает рекомендации мероприятий для пользователя
     *
     * @param userId     идентификатор пользователя
     * @param maxResults максимальное количество результатов
     * @return список рекомендуемых мероприятий с предсказанными оценками
     */
    public List<RecommendedEvent> getRecommendationsForUser(
            final Long userId,
            final Integer maxResults
    ) {
        try {
            log.info("Requesting recommendations for user={}, maxResults={}", userId, maxResults);

            final UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId.intValue())
                    .setMaxResults(maxResults)
                    .build();

            final Iterator<RecommendedEventProto> response = analyzerStub.getRecommendationsForUser(request);

            final List<RecommendedEvent> recommendations = new ArrayList<>();
            while (response.hasNext()) {
                final RecommendedEventProto event = response.next();
                recommendations.add(new RecommendedEvent(
                        (long) event.getEventId(),
                        event.getScore()
                ));
            }

            log.info("Received {} recommendations for user {}", recommendations.size(), userId);
            return recommendations;

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while getting recommendations: {}", e.getStatus(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Failed to get recommendations: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Получает мероприятия, похожие на указанное
     *
     * @param eventId    идентификатор мероприятия
     * @param userId     идентификатор пользователя (для исключения просмотренных)
     * @param maxResults максимальное количество результатов
     * @return список похожих мероприятий с коэффициентами сходства
     */
    public List<RecommendedEvent> getSimilarEvents(
            final Long eventId,
            final Long userId,
            final Integer maxResults
    ) {
        try {
            log.info("Requesting similar events: eventId={}, userId={}, maxResults={}",
                    eventId, userId, maxResults);

            final SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId.intValue())
                    .setUserId(userId.intValue())
                    .setMaxResults(maxResults)
                    .build();

            final Iterator<RecommendedEventProto> response = analyzerStub.getSimilarEvents(request);

            final List<RecommendedEvent> similarEvents = new ArrayList<>();
            while (response.hasNext()) {
                RecommendedEventProto event = response.next();
                similarEvents.add(new RecommendedEvent(
                        (long) event.getEventId(),
                        event.getScore()
                ));
            }

            log.info("Received {} similar events for event {}", similarEvents.size(), eventId);
            return similarEvents;

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while getting similar events: {}", e.getStatus(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Failed to get similar events: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Получает сумму максимальных весов взаимодействий для указанных мероприятий
     *
     * @param eventIds список идентификаторов мероприятий
     * @return список мероприятий с суммой максимальных весов
     */
    public List<RecommendedEvent> getInteractionsCount(final List<Long> eventIds) {
        try {
            log.info("Requesting interaction counts for {} events", eventIds.size());

            final InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds.stream().map(Long::intValue).collect(Collectors.toList()))
                    .build();

            final Iterator<RecommendedEventProto> response = analyzerStub.getInteractionsCount(request);

            final List<RecommendedEvent> counts = new ArrayList<>();
            while (response.hasNext()) {
                RecommendedEventProto event = response.next();
                counts.add(new RecommendedEvent(
                        (long) event.getEventId(),
                        event.getScore()
                ));
            }

            log.info("Received interaction counts for {} events", counts.size());
            return counts;

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while getting interaction counts: {}", e.getStatus(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Failed to get interaction counts: {}", e.getMessage(), e);
            return List.of();
        }
    }
}