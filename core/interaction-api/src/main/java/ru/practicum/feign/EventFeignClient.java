package ru.practicum.feign;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.Event;
import ru.practicum.exception.CategoryIsRelatedToEventException;

import java.util.List;
import java.util.Set;

@FeignClient(name = "event-service", path = "/internal/event")
public interface EventFeignClient {

    @GetMapping("/{eventId}")
    Event findById(@PathVariable @NotNull final Long eventId);

    @GetMapping("/{eventId}/user/{userId}")
    Event findByIdAndInitiatorId(
            @PathVariable @NotNull final Long eventId,
            @PathVariable @NotNull final Long userId
    );

    @GetMapping("/exists/{categoryId}")
    boolean existsByCategoryId(final @PathVariable @NotNull Long categoryId);

    @GetMapping("/{eventId}/exists")
    boolean existsById(@PathVariable @NotNull final Long eventId);

    @GetMapping("/all")
    List<Event> findAllById(final @RequestParam("ids") Set<Long> ids);

    @PostMapping("/all/initiator")
    Page<Event> findAllByInitiatorIdIn(final @RequestParam("ids") List<Long> ids, final Pageable pageable);

    @DeleteMapping(path = "/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEvent(final @PathVariable @Positive Long eventId) throws CategoryIsRelatedToEventException;
}
