package ru.practicum.mapping;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import stats.service.collector.ActionTypeProto;
import stats.service.collector.UserActionProto;

import java.time.Instant;

import static ru.practicum.ewm.stats.avro.ActionTypeAvro.*;

@Component
public class UserActionAvroConverter {

    public UserActionAvro toAvro(final UserActionProto proto) {
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(toActionAvro(proto.getActionType()))
                .setTimestamp(toInstant(proto.getTimestamp()))
                .build();
    }

    private Instant toInstant(final Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }

    private ActionTypeAvro toActionAvro(final ActionTypeProto proto) {
        switch (proto) {
            case ACTION_LIKE -> {
                return LIKE;
            }
            case ACTION_VIEW -> {
                return VIEW;
            }
            case ACTION_REGISTER -> {
                return REGISTER;
            }
            default -> {
                return null;
            }
        }
    }
}
