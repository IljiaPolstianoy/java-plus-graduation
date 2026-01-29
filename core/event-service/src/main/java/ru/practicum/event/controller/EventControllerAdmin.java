package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventFilterAdmin;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.*;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
public class EventControllerAdmin {

    private final EventService eventService;

    @GetMapping
    public List<EventDtoFull> findEvents(@Valid EventFilterAdmin eventFilterAdmin) throws FilterValidationException, EventDateException {
        return eventService.findEventsByUsers(eventFilterAdmin);
    }

    @PatchMapping("{eventId}")
    public EventDtoFull updateEventById(@PathVariable Long eventId,
                                    @RequestBody @Validated(Default.class) EventDto eventDto) throws EventValidationException, EventNotFoundException, EventDateException, EventAlreadyPublishedException, EventCanceledCantPublishException {
        eventDto.setId(eventId);
        return eventService.updateEventById(eventDto);
    }
}
