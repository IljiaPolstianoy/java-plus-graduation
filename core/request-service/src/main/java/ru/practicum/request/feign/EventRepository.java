package ru.practicum.request.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.feign.EventFeignClient;

@Component
@RequiredArgsConstructor
public class EventRepository {

    private final EventFeignClient eventFeignClient;

    public Event findByIdAndInitiatorId(final Long eventId, final Long initiatorId) {
        return eventFeignClient.findByIdAndInitiatorId(eventId, initiatorId);
    }

    public Event findById(final Long eventId) {
        return eventFeignClient.findById(eventId);
    }
}
