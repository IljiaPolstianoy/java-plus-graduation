package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.EventNotFoundException;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
public class EventControllerLike {

    private final EventService eventService;

    /**
     * PUT /events/{eventId}/like — отправляет в Collector информацию о том, что пользователь лайкнул мероприятие
     * Идентификатор пользователя передается в HTTP-заголовке X-EWM-USER-ID
     */
    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public EventDtoFull likeEvent(
            @PathVariable @Positive final Long eventId,
            final HttpServletRequest request
    ) throws EventNotFoundException {
        return eventService.likeEvent(eventId, request);
    }
}