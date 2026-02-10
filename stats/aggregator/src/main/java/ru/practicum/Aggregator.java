package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.practicum.error.WakeupException;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.mapping.EventSimilaritySerializer;
import ru.practicum.mapping.UserActionDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class Aggregator {

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final String TOPIC_OUT = "stats.events-similarity.v1";
    private static final String TOPIC_INPUT = "stats.user-actions.v1";
    private static final List<String> TOPICS_INPUT = List.of(TOPIC_INPUT);

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    // Структуры данных для хранения состояния
    private final Map<Integer, Map<Integer, Double>> userEventWeights = new HashMap<>(); // EventId -> UserId -> MaxWeight
    private final Map<Integer, Double> eventTotalWeights = new HashMap<>(); // EventId -> TotalWeight
    private final Map<Integer, Map<Integer, Double>> minWeightsSums = new HashMap<>(); // EventId(min) -> EventId(max) -> S_min

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        final Properties configConsumer = getConsumerProperties();
        log.info("Получена конфигурация для consumer");
        final Properties configProducer = getProducerProperties();
        log.info("Получена конфигурация для producer");

        final KafkaConsumer<String, UserActionAvro> consumer = new KafkaConsumer<>(configConsumer);
        log.info("Создан consumer для kafka");
        final Producer<String, EventSimilarityAvro> producer = new KafkaProducer<>(configProducer);
        log.info("Создан producer для kafka");

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));

        try {
            consumer.subscribe(TOPICS_INPUT);
            log.info("✅ Aggregator запущен и подписан на топики: {}", TOPICS_INPUT);

            while (true) {
                final ConsumerRecords<String, UserActionAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                if (!records.isEmpty()) {
                    log.info("📥 Получено {} событий от {}", records.count(), TOPIC_INPUT);
                }

                int count = 0;

                for (ConsumerRecord<String, UserActionAvro> recordConsumer : records) {
                    final UserActionAvro userAction = recordConsumer.value();
                    log.info("🔍 Входящее событие: user={}, event={}, action={}, timestamp={}",
                            userAction.getUserId(),
                            userAction.getEventId(),
                            userAction.getActionType(),
                            userAction.getTimestamp()
                    );

                    log.info("Обновляем состояние и получаем сообщения о схожести");
                    final List<EventSimilarityAvro> similarityMessages = updateState(userAction);

                    log.info("Отправляем все сообщения о схожести");
                    for (EventSimilarityAvro similarityMessage : similarityMessages) {
                        final String key = similarityMessage.getEventA() + "_" + similarityMessage.getEventB();
                        final ProducerRecord<String, EventSimilarityAvro> recordProducer =
                                new ProducerRecord<>(TOPIC_OUT, key, similarityMessage);

                        producer.send(recordProducer, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("❌ Ошибка отправки сообщения о схожести: {}", exception.getMessage());
                            } else {
                                log.debug("✅ Отправлено сообщение о схожести: eventA={}, eventB={}, score={}",
                                        similarityMessage.getEventA(),
                                        similarityMessage.getEventB(),
                                        similarityMessage.getScore());
                            }
                        });
                    }

                    manageOffsets(recordConsumer, count, consumer);
                    count++;
                }

                if (!records.isEmpty()) {
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {
            log.info("WakeupException получен, завершаем работу Aggregator");
        } catch (Exception e) {
            log.error("❌ Ошибка во время обработки событий от {}", TOPIC_INPUT, e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
                log.info("✅ Все сообщения отправлены, оффсеты зафиксированы");
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
                log.info("Aggregator полностью остановлен");
            }
        }
    }

    private Properties getConsumerProperties() {
        final Properties properties = new Properties();

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "AggregationConsumer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregation.group.id");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return properties;
    }

    private static Properties getProducerProperties() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSimilaritySerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        return config;
    }

    private static void manageOffsets(
            final ConsumerRecord<String, UserActionAvro> record,
            int count,
            final KafkaConsumer<String, UserActionAvro> consumer) {

        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    /**
     * Получение веса для типа действия
     */
    private double getWeightForAction(final ActionTypeAvro actionType) {
        return ACTION_WEIGHTS.getOrDefault(actionType, 0.0);
    }

    /**
     * Основной метод обновления состояния агрегатора
     * Возвращает список сообщений о схожести, которые нужно отправить
     */
    private List<EventSimilarityAvro> updateState(final UserActionAvro userAction) {
        final List<EventSimilarityAvro> similarityMessages = new ArrayList<>();

        final int userId = userAction.getUserId();
        final int eventId = userAction.getEventId();
        final ActionTypeAvro actionType = userAction.getActionType();
        final long timestamp = userAction.getTimestamp().toEpochMilli();

        // Получаем вес для типа действия
        final double actionWeight = getWeightForAction(actionType);
        if (actionWeight <= 0) {
            log.warn("Неизвестный тип действия или нулевой вес: {}", actionType);
            return similarityMessages;
        }

        // Получаем текущий максимальный вес пользователя для этого мероприятия
        final double currentMaxWeight = getUserMaxWeightForEvent(userId, eventId);

        // Если новый вес не больше текущего максимального, пересчет не требуется
        if (actionWeight <= currentMaxWeight) {
            log.debug("Вес {} не больше текущего максимального {}. Пересчет не требуется.",
                    actionWeight, currentMaxWeight);
            return similarityMessages;
        }

        log.debug("Обновляем вес: user={}, event={}, action={}, oldWeight={}, newWeight={}",
                userId, eventId, actionType, currentMaxWeight, actionWeight);

        // Вычисляем разницу весов
        final double weightDiff = actionWeight - currentMaxWeight;

        // Обновляем максимальный вес пользователя для мероприятия
        updateUserEventWeight(userId, eventId, actionWeight);

        // Обновляем общую сумму весов для мероприятия
        updateEventTotalWeight(eventId, weightDiff);

        // Для каждого другого мероприятия обновляем S_min и пересчитываем сходство
        for (Integer otherEventId : eventTotalWeights.keySet()) {
            if (otherEventId.equals(eventId)) {
                continue;
            }

            // Обновляем S_min для пары мероприятий
            updateMinWeightsSum(eventId, otherEventId, userId, actionWeight, currentMaxWeight);

            // Рассчитываем новое сходство
            final EventSimilarityAvro similarityMessage = calculateSimilarity(eventId, otherEventId, timestamp);
            if (similarityMessage != null) {
                similarityMessages.add(similarityMessage);
            }
        }

        // Если это первое взаимодействие с мероприятием, нужно также обработать как новое мероприятие
        if (currentMaxWeight == 0.0) {
            handleNewEvent(eventId, userId, actionWeight, timestamp, similarityMessages);
        }

        return similarityMessages;
    }

    /**
     * Получение максимального веса пользователя для мероприятия
     */
    private double getUserMaxWeightForEvent(final int userId, final int eventId) {
        final Map<Integer, Double> userWeights = userEventWeights.get(eventId);
        if (userWeights == null) {
            return 0.0;
        }
        return userWeights.getOrDefault(userId, 0.0);
    }

    /**
     * Обновление веса пользователя для мероприятия
     */
    private void updateUserEventWeight(final int userId, final int eventId, final double newWeight) {
        userEventWeights
                .computeIfAbsent(eventId, k -> new HashMap<>())
                .put(userId, newWeight);
    }

    /**
     * Обновление общей суммы весов для мероприятия
     */
    private void updateEventTotalWeight(final int eventId, final double weightDiff) {
        final double currentTotal = eventTotalWeights.getOrDefault(eventId, 0.0);
        eventTotalWeights.put(eventId, currentTotal + weightDiff);
    }

    /**
     * Обновление суммы минимальных весов для пары мероприятий
     */
    private void updateMinWeightsSum(final int eventId1,final  int eventId2,
                                     final int updatedUserId, final double newWeight, final double oldWeight) {
        final int first = Math.min(eventId1, eventId2);
        final int second = Math.max(eventId1, eventId2);

        // Получаем текущее значение S_min
        final double currentSMin = getMinWeightsSum(first, second);

        // Получаем вес пользователя для второго мероприятия
        final double weightForOtherEvent = getUserMaxWeightForEvent(updatedUserId, eventId2);

        // Если пользователь взаимодействовал с обоими мероприятиями
        if (weightForOtherEvent > 0) {
            // Пересчитываем минимальный вес для этого пользователя
            final double oldMin = Math.min(oldWeight, weightForOtherEvent);
            final double newMin = Math.min(newWeight, weightForOtherEvent);
            final double minDiff = newMin - oldMin;

            // Обновляем S_min
            minWeightsSums
                    .computeIfAbsent(first, k -> new HashMap<>())
                    .put(second, currentSMin + minDiff);

            log.debug("Обновлен S_min для пары ({}, {}): oldMin={}, newMin={}, diff={}, total={}",
                    first, second, oldMin, newMin, minDiff, currentSMin + minDiff);
        }
        // Если пользователь не взаимодействовал со вторым мероприятием, S_min не меняется
    }

    /**
     * Получение суммы минимальных весов для пары мероприятий
     */
    private double getMinWeightsSum(final int eventId1, final int eventId2) {
        final int first = Math.min(eventId1, eventId2);
        final int second = Math.max(eventId1, eventId2);

        final Map<Integer, Double> innerMap = minWeightsSums.get(first);
        if (innerMap == null) {
            return 0.0;
        }
        return innerMap.getOrDefault(second, 0.0);
    }

    /**
     * Расчет сходства для пары мероприятий
     */
    private EventSimilarityAvro calculateSimilarity(final int eventId1, final int eventId2, final long timestamp) {
        final int first = Math.min(eventId1, eventId2);
        final int second = Math.max(eventId1, eventId2);

        // Получаем необходимые значения
        final Double totalWeight1 = eventTotalWeights.get(eventId1);
        final Double totalWeight2 = eventTotalWeights.get(eventId2);
        final Double sMin = getMinWeightsSum(first, second);

        if (totalWeight1 == null || totalWeight2 == null || totalWeight1 <= 0 || totalWeight2 <= 0) {
            return null;
        }

        // Рассчитываем косинусное сходство
        final double similarity = calculateCosineSimilarity(sMin, totalWeight1, totalWeight2);

        log.debug("Рассчитано сходство: eventA={}, eventB={}, S_min={}, S1={}, S2={}, similarity={}",
                first, second, sMin, totalWeight1, totalWeight2, similarity);

        // Создаем сообщение
        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(Instant.ofEpochMilli(timestamp))
                .build();
    }

    /**
     * Расчет косинусного сходства
     */
    private double calculateCosineSimilarity(final double sMin, final double totalWeight1, final double totalWeight2) {
        double denominator = Math.sqrt(totalWeight1 * totalWeight2);
        if (denominator == 0) {
            return 0.0;
        }
        return sMin / denominator;
    }

    /**
     * Обработка нового мероприятия (когда появляется первый пользователь)
     */
    private void handleNewEvent(final int newEventId, final int userId, final double weight, final long timestamp,
                                final List<EventSimilarityAvro> similarityMessages) {
        log.info("Обработка нового мероприятия: event={}, первый пользователь={}", newEventId, userId);

        // Для каждого существующего мероприятия
        for (Integer existingEventId : eventTotalWeights.keySet()) {
            if (existingEventId.equals(newEventId)) {
                continue;
            }

            // Инициализируем S_min = 0 для новой пары
            final int first = Math.min(newEventId, existingEventId);
            final int second = Math.max(newEventId, existingEventId);

            minWeightsSums
                    .computeIfAbsent(first, k -> new HashMap<>())
                    .put(second, 0.0);

            // Рассчитываем и добавляем сходство (будет 0, так как нет общих пользователей)
            final EventSimilarityAvro similarityMessage = calculateSimilarity(newEventId, existingEventId, timestamp);
            if (similarityMessage != null) {
                similarityMessages.add(similarityMessage);
            }
        }
    }
}