package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.storage.EventSimilarityRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityConsumerService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            groupId = "analyzer-event-similarity-group",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(EventSimilarityAvro similarity) {
        log.info("Received similarity: eventA={}, eventB={}, score={}, timestamp={}",
                similarity.getEventA(), similarity.getEventB(),
                similarity.getScore(), similarity.getTimestamp());

        try {
            eventSimilarityRepository.findByEventAIdAndEventBId(
                    similarity.getEventA(),
                    similarity.getEventB()
            ).ifPresentOrElse(
                    existing -> {
                        existing.setScore(similarity.getScore());
                        existing.setUpdatedAt(similarity.getTimestamp());
                        eventSimilarityRepository.save(existing);
                        log.debug("Updated existing similarity for pair ({}, {})",
                                similarity.getEventA(), similarity.getEventB());
                    },
                    () -> {
                        EventSimilarity newSimilarity = new EventSimilarity();
                        newSimilarity.setEventAId(similarity.getEventA());
                        newSimilarity.setEventBId(similarity.getEventB());
                        newSimilarity.setScore(similarity.getScore());
                        newSimilarity.setUpdatedAt(similarity.getTimestamp());
                        eventSimilarityRepository.save(newSimilarity);
                        log.debug("Saved new similarity for pair ({}, {})",
                                similarity.getEventA(), similarity.getEventB());
                    }
            );
        } catch (Exception e) {
            log.error("Failed to process similarity message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save event similarity", e);
        }
    }
}