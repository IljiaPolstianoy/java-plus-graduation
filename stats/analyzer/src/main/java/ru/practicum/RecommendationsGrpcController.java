package ru.practicum;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsGrpcController
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(
            final UserPredictionsRequestProto request,
            final StreamObserver<RecommendedEventProto> responseObserver
    ) {

        final int userId = request.getUserId();
        final int maxResults = request.getMaxResults();

        log.info("gRPC request: getRecommendationsForUser(userId={}, maxResults={})",
                userId, maxResults);

        try {
            final List<RecommendationService.RecommendedEvent> predictions =
                    recommendationService.predictForUser(userId, maxResults);

            for (RecommendationService.RecommendedEvent prediction : predictions) {
                RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(prediction.getEventId())
                        .setScore(prediction.getScore())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("Sent {} recommendations for user {}", predictions.size(), userId);

        } catch (Exception e) {
            log.error("Error getting recommendations for user {}: {}", userId, e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(
            final SimilarEventsRequestProto request,
            final StreamObserver<RecommendedEventProto> responseObserver) {

        final int eventId = request.getEventId();
        final int userId = request.getUserId();
        final int maxResults = request.getMaxResults();

        log.info("gRPC request: getSimilarEvents(eventId={}, userId={}, maxResults={})",
                eventId, userId, maxResults);

        try {
            final List<RecommendationService.RecommendedEvent> similarEvents =
                    recommendationService.findSimilarEvents(eventId, userId, maxResults);

            for (RecommendationService.RecommendedEvent event : similarEvents) {
                final RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(event.getEventId())
                        .setScore(event.getScore())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("Sent {} similar events for event {}", similarEvents.size(), eventId);

        } catch (Exception e) {
            log.error("Error getting similar events for event {}: {}", eventId, e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(
            final InteractionsCountRequestProto request,
            final StreamObserver<RecommendedEventProto> responseObserver) {

        final List<Integer> eventIds = request.getEventIdList();
        log.info("gRPC request: getInteractionsCount({} events)", eventIds.size());

        try {
            final List<RecommendationService.RecommendedEvent> counts =
                    recommendationService.getInteractionsCount(eventIds);

            for (RecommendationService.RecommendedEvent count : counts) {
                final RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(count.getEventId())
                        .setScore(count.getScore())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("Sent interaction counts for {} events", counts.size());

        } catch (Exception e) {
            log.error("Error getting interaction counts: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}