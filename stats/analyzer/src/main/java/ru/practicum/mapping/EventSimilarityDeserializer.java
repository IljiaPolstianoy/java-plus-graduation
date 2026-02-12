package ru.practicum.mapping;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.error.DeserializationException;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Map;

public class EventSimilarityDeserializer implements Deserializer<EventSimilarityAvro> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final DatumReader<EventSimilarityAvro> reader;

    public EventSimilarityDeserializer() {
        this.reader = new SpecificDatumReader<>(EventSimilarityAvro.class);
    }

    public EventSimilarityDeserializer(final DatumReader<EventSimilarityAvro> reader) {
        this.reader = reader;
    }

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
    }

    @Override
    public EventSimilarityAvro deserialize(final String topic, final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            final BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new DeserializationException(
                    String.format("Ошибка десериализации EventSimilarityAvro из топика [%s]", topic),
                    e
            );
        }
    }

    @Override
    public void close() {
    }
}