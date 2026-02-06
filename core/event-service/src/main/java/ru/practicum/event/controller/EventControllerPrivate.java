package ru.practicum.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.*;
import ru.practicum.feign.RequestService;
import ru.practicum.validation.ValidationGroups;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventControllerPrivate {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDtoFull createEvent(@PathVariable @Positive Long userId, @RequestBody @Validated({ValidationGroups.Create.class, Default.class}) EventDto eventDto) throws UserNotFoundException, CategoryNotFoundException, EventDateException, EventValidationException {
        eventDto.setInitiator(userId);
        return eventService.createEvent(eventDto);
    }

    @GetMapping
    public List<EventDtoFull> findEventsByUser(@PathVariable @Positive Long userId,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {

        return eventService.findEventsByUserid(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventDtoFull findEventByUserId(@PathVariable @Positive Long userId, @PathVariable @Positive Long eventId) throws EventNotFoundException {

        return eventService.findEventByUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventDtoFull updateEventByUserId(@PathVariable Long userId, @PathVariable Long eventId,
                                            @RequestBody @Validated(Default.class) EventDto eventDto) throws EventNotFoundException, EventDateException, EventCanceledCantPublishException {
        eventDto.setInitiator(userId);
        eventDto.setId(eventId);
        return eventService.updateEventByUserId(eventDto);
    }

    /**
     *  Получение информации о запросах на участие в событии текущего пользователя
     */
    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequestsByOwnerOfEvent(@PathVariable @Positive Long userId, @PathVariable @Positive Long eventId) throws EventNotFoundException {
        return requestService.getRequestsByOwnerOfEvent(userId, eventId);
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     */
    @PatchMapping("/{eventId}/requests")
    public RequestStatusUpdateResultDto updateRequests(@PathVariable @Positive Long userId,
                                                       @PathVariable @Positive Long eventId,
                                                       @RequestBody @Validated RequestStatusUpdateDto requestStatusUpdateDto) throws EventNotFoundException, EventNotPublishedException, ParticipantLimitExceededException {
        return requestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }

}
