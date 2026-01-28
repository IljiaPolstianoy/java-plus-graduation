package ru.practicum.request.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.feign.EventFeignClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventRepository {

    private final EventFeignClient eventFeignClient;

    public Optional<Event> findByIdAndInitiatorId(final Long eventId, final Long initiatorId) {
        return eventFeignClient.findByIdAndInitiatorId(eventId, initiatorId);
    }

    public Optional<Event> findById(final Long eventId) {
        return eventFeignClient.findById(eventId);
    }
}
