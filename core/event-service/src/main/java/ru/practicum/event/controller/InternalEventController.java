package ru.practicum.event.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.Event;
import ru.practicum.dto.EventDtoFull;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.CategoryIsRelatedToEventException;
import ru.practicum.exception.EventNotFoundException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/internal/event")
@RequiredArgsConstructor
@Validated
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventDtoFull findById(@PathVariable @NotNull final Long eventId) {
        return eventService.findById(eventId);
    }

    @GetMapping("/{eventId}/user/{userId}")
    public Event findByIdAndInitiatorId(
            @PathVariable @NotNull final Long eventId,
            @PathVariable @NotNull final Long userId
    ) {
        return eventService.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException("EventId = %d by userId = %d".formatted(eventId, userId)));
    }

    @GetMapping("/exists/{categoryId}")
    public boolean existsByCategoryId(final @PathVariable @NotNull Long categoryId) {
        return eventService.existsByCategoryId(categoryId);
    }

    @GetMapping("/{eventId}/exists")
    public boolean existsById(@PathVariable @NotNull final Long eventId) {
        return eventService.existsById(eventId);
    }

    @GetMapping("/all")
    public List<Event> findAllById(final @RequestParam("ids") Set<Long> ids) {
        return eventService.findAllById(ids);
    }

    @GetMapping("/all/initiator")
    public Page<Event> findAllByInitiatorIdIn(
            final @RequestParam("ids") List<Long> userIds, Pageable pageable
    ) {
        return eventService.findAllByInitiatorIdIn(userIds, pageable);
    }


    @DeleteMapping(path = "/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(final @PathVariable @Positive Long eventId) throws CategoryIsRelatedToEventException {
        eventService.delete(eventId);
    }
}
