package ru.practicum.mapping;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import ru.practicum.error.SerializationException;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EventSimilaritySerializer implements Serializer<EventSimilarityAvro> {

    private final EncoderFactory encoderFactory;
    private final DatumWriter<EventSimilarityAvro> datumWriter;

    public EventSimilaritySerializer() {
        this(EncoderFactory.get(), EventSimilarityAvro.getClassSchema());
    }

    private EventSimilaritySerializer(EncoderFactory encoderFactory, Schema schema) {
        this.encoderFactory = encoderFactory;
        this.datumWriter = new SpecificDatumWriter<>(schema);
    }

    @Override
    public byte[] serialize(String topic, EventSimilarityAvro event) {
        if (event == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            final BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);

            datumWriter.write(event, encoder);
            encoder.flush();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации экземпляра SensorEventAvro", e);
        }
    }
}
