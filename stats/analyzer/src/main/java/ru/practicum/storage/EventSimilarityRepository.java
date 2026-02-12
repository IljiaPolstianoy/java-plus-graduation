package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventSimilarity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    // Для получения похожих мероприятий (двусторонний поиск)
    @Query("SELECT es FROM EventSimilarity es " +
            "WHERE es.eventAId = :eventId OR es.eventBId = :eventId " +
            "ORDER BY es.score DESC")
    List<EventSimilarity> findAllSimilarEvents(@Param("eventId") Integer eventId);

    // Для получения конкретной пары
    Optional<EventSimilarity> findByEventAIdAndEventBId(Integer eventAId, Integer eventBId);

    // Для обновления/вставки
    @Query("UPDATE EventSimilarity es SET es.score = :score, es.updatedAt = :updatedAt " +
            "WHERE es.eventAId = :eventAId AND es.eventBId = :eventBId")
    void updateScore(@Param("eventAId") Integer eventAId,
                     @Param("eventBId") Integer eventBId,
                     @Param("score") Double score,
                     @Param("updatedAt") Instant updatedAt);
}



