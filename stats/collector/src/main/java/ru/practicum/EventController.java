package ru.practicum;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.kafka.SendKafka;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;

@GrpcService
@RequiredArgsConstructor
public class EventController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final SendKafka sendKafka;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            sendKafka.send(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
