package ru.practicum.mapping;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.error.DeserializationException;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserializer implements Deserializer<UserActionAvro> {

    private final DecoderFactory decoderFactory;
    private final DatumReader<UserActionAvro> reader;

    public UserActionDeserializer() {
        this(DecoderFactory.get(), UserActionAvro.getClassSchema());
    }

    private UserActionDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                final BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
                return this.reader.read(null, decoder);
            }
            return null;
        } catch (Exception e) {
            throw new DeserializationException("Ошибка десериализации данных из топика [" + topic + "]", e);
        }
    }
}
