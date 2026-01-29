package ru.practicum.feign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.event.Event;
import ru.practicum.event.RequestAllByInitiatorIds;
import ru.practicum.event.RequestAllEvent;

import java.util.List;
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

    @GetMapping("/{eventId}/exists")
    boolean existsById(@PathVariable @NotNull final Long eventId);

    @GetMapping("/all")
    List<Event> findAllById(final RequestAllEvent requestAllEvent);

    @GetMapping("/all/initiator")
    Page<Event> findAllByInitiatorIdIn(final @RequestBody @Valid RequestAllByInitiatorIds requestAllByInitiatorIds);
}
