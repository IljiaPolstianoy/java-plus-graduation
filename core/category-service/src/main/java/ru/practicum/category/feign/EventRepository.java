package ru.practicum.category.feign;

import org.springframework.stereotype.Component;
import ru.practicum.feign.EventFeignClient;

@Component
public class EventRepository {

    private EventFeignClient eventFeignClient;

    public boolean existsByCategoryId(final Long categoryId) {
        return eventFeignClient.existsByCategoryId(categoryId);
    }
}
