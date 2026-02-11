package ru.practicum.mapping;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import ru.practicum.error.SerializationException;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UserActionAvroSerialization implements Serializer<UserActionAvro> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(final String topic, final UserActionAvro data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            final BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
            final DatumWriter<UserActionAvro> datumWriter = new SpecificDatumWriter<>(UserActionAvro.class);

            datumWriter.write(data, encoder);
            encoder.flush();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации экземпляра SensorEventAvro", e);
        }
    }
}
