package ru.practicum.mainservice.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.event.RequestAllByInitiatorIds;
import ru.practicum.event.RequestAllEvent;
import ru.practicum.feign.EventFeignClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EventRepository {

    private final EventFeignClient eventFeignClient;

    public boolean existsById(final Long eventId) {
        return eventFeignClient.existsById(eventId);
    }

    public Optional<Event> findById(final Long eventId) {
        return eventFeignClient.findById(eventId);
    }

    public List<Event> findAllById(final Set<Long> ids) {
        return eventFeignClient.findAllById(RequestAllEvent.builder().ids(ids).build());
    }

    public Page<Event> findAllByInitiatorIdIn(List<Long> userId, Pageable pageable) {
        return eventFeignClient.findAllByInitiatorIdIn(new RequestAllByInitiatorIds(userId, pageable));
    }
}
