package ru.practicum.feign;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.Event;

import java.util.Optional;

@FeignClient(name = "event-service", path = "/internal/event")
public interface EventFeignClient {

    @GetMapping("/{eventId}")
    Optional<Event> findById(@PathVariable @NotNull final Long eventId);

    @GetMapping("/{eventId}/user/{userId}")
    Optional<Event> findByIdAndInitiatorId(
            @PathVariable @NotNull final Long eventId,
            @PathVariable @NotNull final Long userId
    );

    @GetMapping("/exists/{categoryId}")
    boolean existsByCategoryId(final @PathVariable @NotNull Long categoryId);
}
