package ru.practicum.event.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.event.Event;
import ru.practicum.event.service.EventService;

import java.util.Optional;

@Controller
@RequestMapping("/internal/event")
@RequiredArgsConstructor
@Validated
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public Optional<Event> findById(@PathVariable @NotNull final Long eventId) {
        return eventService.findById(eventId);
    }

    @GetMapping("/{eventId}/user/{userId}")
    public Optional<Event> findByIdAndInitiatorId(
            @PathVariable @NotNull final Long eventId,
            @PathVariable @NotNull final Long userId
    ) {
        return eventService.findByIdAndInitiatorId(eventId, userId);
    }

    @GetMapping("/exists/{categoryId}")
    public boolean existsByCategoryId(final @PathVariable @NotNull Long categoryId) {
        return eventService.existsByCategoryId(categoryId);
    }
}
