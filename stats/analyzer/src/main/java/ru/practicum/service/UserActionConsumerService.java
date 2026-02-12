package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserEventInteraction;
import ru.practicum.storage.ActionWeightRepository;
import ru.practicum.storage.UserEventInteractionRepository;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionConsumerService {

    private final UserEventInteractionRepository interactionRepository;
    private final ActionWeightRepository actionWeightRepository;

    /**
     * Потребляет сообщения из топика stats.user-actions.v1
     * Обновляет таблицу истории взаимодействий пользователей с мероприятиями
     * Сохраняется ТОЛЬКО максимальный вес для каждой пары (user_id, event_id)
     */
    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "${spring.kafka.consumer.group-id:analyzer-group}.user-action",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(final UserActionAvro userAction) {
        log.info("Received user action: user={}, event={}, action={}, timestamp={}",
                userAction.getUserId(),
                userAction.getEventId(),
                userAction.getActionType(),
                userAction.getTimestamp()
        );

        try {
            // 1. Преобразуем Avro ActionType в внутренний Enum
            final ActionType actionType = mapToActionType(userAction.getActionType());

            // 2. Получаем вес для данного типа действия
            final Double weight = getWeightForAction(actionType);

            // 3. Получаем timestamp из сообщения
            final Instant timestamp = userAction.getTimestamp();

            // 4. Сохраняем или обновляем взаимодействие
            upsertInteraction(
                    userAction.getUserId(),
                    userAction.getEventId(),
                    actionType,
                    weight,
                    timestamp
            );

            log.debug("Successfully processed user action: user={}, event={}, weight={}",
                    userAction.getUserId(), userAction.getEventId(), weight);

        } catch (Exception e) {
            log.error("Failed to process user action: user={}, event={}, error={}",
                    userAction.getUserId(), userAction.getEventId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save user interaction", e);
        }
    }

    /**
     * Преобразует ActionTypeAvro из Avro-схемы во внутренний Enum ActionType
     */
    private ActionType mapToActionType(final ActionTypeAvro avro) {
        return switch (avro) {
            case VIEW -> ActionType.VIEW;
            case REGISTER -> ActionType.REGISTER;
            case LIKE -> ActionType.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + avro);
        };
    }

    /**
     * Получает вес для типа действия из справочника в БД
     */
    private Double getWeightForAction(final ActionType actionType) {
        return actionWeightRepository.findByActionType(actionType)
                .orElseThrow(() -> new IllegalArgumentException("No weight configured for action type: " + actionType))
                .getWeight();
    }

    /**
     * Вставка или обновление взаимодействия
     * Сохраняется ТОЛЬКО максимальный вес для пары (user_id, event_id)
     */
    private void upsertInteraction(
            final Integer userId,
            final Integer eventId,
            final ActionType actionType,
            final Double weight,
            final Instant timestamp
    ) {

        interactionRepository.findByUserIdAndEventId(userId, eventId)
                .ifPresentOrElse(
                        // Существующая запись - обновляем только если новый вес больше
                        existing -> {
                            if (weight > existing.getWeight()) {
                                existing.setWeight(weight);
                                existing.setActionType(actionType);
                                existing.setTimestamp(timestamp);
                                interactionRepository.save(existing);
                                log.debug("Updated interaction: user={}, event={}, new weight={} (old={})",
                                        userId, eventId, weight, existing.getWeight());
                            } else if (weight < existing.getWeight()) {
                                log.debug("Skipped update: new weight={} <= existing weight={} for user={}, event={}",
                                        weight, existing.getWeight(), userId, eventId);
                            } else {
                                // Вес равен существующему - обновляем только timestamp
                                existing.setTimestamp(timestamp);
                                interactionRepository.save(existing);
                                log.debug("Updated timestamp only for user={}, event={}", userId, eventId);
                            }
                        },
                        // Нет записи - создаем новую
                        () -> {
                            final UserEventInteraction newInteraction = new UserEventInteraction();
                            newInteraction.setUserId(userId);
                            newInteraction.setEventId(eventId);
                            newInteraction.setActionType(actionType);
                            newInteraction.setWeight(weight);
                            newInteraction.setTimestamp(timestamp);
                            interactionRepository.save(newInteraction);
                            log.debug("Created new interaction: user={}, event={}, weight={}",
                                    userId, eventId, weight);
                        }
                );
    }
}