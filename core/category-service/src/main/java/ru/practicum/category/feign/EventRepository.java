package ru.practicum.category.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.feign.EventFeignClient;

@Component
@RequiredArgsConstructor
public class EventRepository {

    private final EventFeignClient eventFeignClient;

    public boolean existsByCategoryId(final Long categoryId) {
        return eventFeignClient.existsByCategoryId(categoryId);
    }

    public void delete(final Long eventId) {
        eventFeignClient.deleteEvent(eventId);
    }
}
