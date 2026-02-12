package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;
import ru.practicum.error.SerializationException;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapping.UserActionAvroConverter;
import ru.practicum.mapping.UserActionAvroSerialization;
import stats.service.collector.UserActionProto;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendKafkaImpl implements SendKafka {

    private final UserActionAvroConverter userActionAvroConverter;
    private final Producer<String, UserActionAvro> producer = createProducer();

    @Override
    public boolean send(UserActionProto request) {
        final String methodName = "send";
        log.info("[{}] Начало отправки сообщения в Kafka. Request: {}", methodName, request);

        try {
            log.debug("[{}] Конвертация UserActionProto в UserActionAvro", methodName);
            final UserActionAvro userActionAvro = userActionAvroConverter.toAvro(request);
            log.debug("[{}] Конвертация успешна. Avro объект: {}", methodName, userActionAvro);

            final String topic = "stats.user-actions.v1";
            final ProducerRecord<String, UserActionAvro> recordUserAction =
                    new ProducerRecord<>(topic, userActionAvro);

            log.info("[{}] Отправка сообщения в топик: {}. Key: {}, Value размер: {} байт",
                    methodName, topic, recordUserAction.key(),
                    userActionAvro.toString().getBytes().length);

            producer.send(recordUserAction);
            return true;
        } catch (Exception e) {
            log.error("[{}] Неожиданная ошибка при отправке в Kafka. Request: {}",
                    methodName, request, e);
            throw new SerializationException("Неожиданная ошибка отправки UserActionProto в Kafka", e);
        } finally {
            log.debug("[{}] Завершение метода send. Request обработан", methodName);
        }
    }

    @PreDestroy
    public void close() {
        final String methodName = "close";
        log.info("[{}] Начало закрытия Kafka producer", methodName);

        if (producer != null) {
            try {
                log.debug("[{}] Статистика producer перед закрытием: {}",
                        methodName, producer.metrics());

                producer.close();
                log.info("[{}] Kafka producer успешно закрыт", methodName);
            } catch (Exception e) {
                log.error("[{}] Ошибка при закрытии Kafka producer", methodName, e);
            }
        } else {
            log.warn("[{}] Kafka producer уже null, закрытие не требуется", methodName);
        }
    }

    private Producer<String, UserActionAvro> createProducer() {
        final String methodName = "createProducer";
        log.info("[{}] Создание Kafka producer", methodName);

        final Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserActionAvroSerialization.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        log.debug("[{}] Конфигурация Kafka producer:", methodName);
        config.forEach((key, value) ->
                log.debug("[{}]   {} = {}", methodName, key, value));

        try {
            final KafkaProducer<String, UserActionAvro> producer = new KafkaProducer<>(config);
            log.info("[{}] Kafka producer успешно создан. Client ID: {}",
                    methodName, config.get(ProducerConfig.CLIENT_ID_CONFIG));

            log.debug("[{}] Начальная статистика producer: {}", methodName, producer.metrics());

            return producer;
        } catch (Exception e) {
            log.error("[{}] Ошибка создания Kafka producer", methodName, e);
            throw new RuntimeException("Не удалось создать Kafka producer", e);
        }
    }
}