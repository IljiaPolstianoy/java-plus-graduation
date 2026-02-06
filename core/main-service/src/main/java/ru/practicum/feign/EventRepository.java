package ru.practicum.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EventRepository {

    private final EventFeignClient eventFeignClient;
    private final EventMapper eventMapper;

    public boolean existsById(final Long eventId) {
        return eventFeignClient.existsById(eventId);
    }

    public Event findById(final Long eventId) {
        return eventMapper.toEventFromFullDto(eventFeignClient.findById(eventId));
    }

    public List<Event> findAllById(final Set<Long> ids) {
        return eventFeignClient.findAllById(ids);
    }

    public Page<Event> findAllByInitiatorIdIn(List<Long> userId, Pageable pageable) {
        return eventFeignClient.findAllByInitiatorIdIn(userId, pageable);
    }
}
