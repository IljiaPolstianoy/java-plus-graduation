package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserEventInteraction;

import java.util.List;
import java.util.Optional;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteraction, Long> {

    List<UserEventInteraction> findByUserIdOrderByTimestampDesc(Integer userId);

    @Query(value = "SELECT * FROM user_event_interaction " +
            "WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit",
            nativeQuery = true)
    List<UserEventInteraction> findRecentByUserId(@Param("userId") Integer userId,
                                                  @Param("limit") int limit);

    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);

    Optional<UserEventInteraction> findByUserIdAndEventId(Integer userId, Integer eventId);

    @Query("SELECT uei FROM UserEventInteraction uei " +
            "WHERE uei.userId = :userId AND uei.eventId IN :eventIds")
    List<UserEventInteraction> findByUserIdAndEventIdIn(@Param("userId") Integer userId,
                                                        @Param("eventIds") List<Integer> eventIds);

    @Query(value = "SELECT SUM(max_weight) FROM (" +
            "   SELECT MAX(weight) as max_weight " +
            "   FROM user_event_interaction " +
            "   WHERE event_id = :eventId " +
            "   GROUP BY user_id" +
            ") AS user_max_weights",
            nativeQuery = true)
    Double sumMaxWeightsByEventId(@Param("eventId") Integer eventId);

    @Query(value = "SELECT event_id, SUM(max_weight) FROM (" +
            "   SELECT event_id, user_id, MAX(weight) as max_weight " +
            "   FROM user_event_interaction " +
            "   WHERE event_id IN :eventIds " +
            "   GROUP BY event_id, user_id" +
            ") AS user_max_weights " +
            "GROUP BY event_id",
            nativeQuery = true)
    List<Object[]> sumMaxWeightsByEventIds(@Param("eventIds") List<Integer> eventIds);
}