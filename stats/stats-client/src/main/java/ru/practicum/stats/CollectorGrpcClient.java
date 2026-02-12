package ru.practicum.stats;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import stats.service.collector.ActionTypeProto;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorGrpcClient {

    @GrpcClient(value = "collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    /**
     * Отправляет информацию о действии пользователя в Collector через gRPC
     */
    public boolean collectUserAction(
            final Long userId,
            final Long eventId,
            final String actionType,
            final LocalDateTime timestamp
    ) {
        try {
            log.info("Sending user action to Collector: user={}, event={}, action={}, timestamp={}",
                    userId, eventId, actionType, timestamp);

            final UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId.intValue())
                    .setEventId(eventId.intValue())
                    .setActionType(mapToActionTypeProto(actionType))
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(timestamp.toEpochSecond(ZoneOffset.UTC))
                            .setNanos(timestamp.getNano())
                            .build())
                    .build();

            final Empty response = collectorStub.collectUserAction(request);
            log.debug("Successfully sent user action to Collector");
            return true;

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while sending action to Collector: {}", e.getStatus(), e);
            return false;
        } catch (Exception e) {
            log.error("Failed to send action to Collector: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Отправляет информацию о просмотре мероприятия
     */
    public boolean sendViewAction(
            final Long userId,
            final Long eventId,
            final LocalDateTime timestamp
    ) {
        return collectUserAction(userId, eventId, "VIEW", timestamp);
    }

    /**
     * Отправляет информацию о регистрации на мероприятие
     */
    public boolean sendRegisterAction(
            final Long userId,
            final Long eventId,
            final LocalDateTime timestamp
    ) {
        return collectUserAction(userId, eventId, "REGISTER", timestamp);
    }

    /**
     * Отправляет информацию о лайке мероприятия
     */
    public boolean sendLikeAction(
            final Long userId,
            final Long eventId,
            final LocalDateTime timestamp
    ) {
        return collectUserAction(userId, eventId, "LIKE", timestamp);
    }

    private ActionTypeProto mapToActionTypeProto(final String actionType) {
        return switch (actionType.toUpperCase()) {
            case "VIEW" -> ActionTypeProto.ACTION_VIEW;
            case "REGISTER" -> ActionTypeProto.ACTION_REGISTER;
            case "LIKE" -> ActionTypeProto.ACTION_LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
        };
    }
}