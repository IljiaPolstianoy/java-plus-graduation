package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import ru.practicum.event.Event;
import ru.practicum.event.RequestAllByInitiatorIds;
import ru.practicum.event.RequestAllEvent;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.dto.EventFilterAdmin;
import ru.practicum.event.dto.EventFilterPublic;
import ru.practicum.exception.*;

import java.util.List;
import java.util.Optional;

public interface EventService {
    // Admin
    List<EventDtoFull> findEventsByUsers(EventFilterAdmin eventFilter) throws FilterValidationException, EventDateException;

    EventDtoFull updateEventById(EventDto eventDto) throws EventNotFoundException, EventValidationException, EventDateException, EventAlreadyPublishedException, EventCanceledCantPublishException;

    // Private
    EventDtoFull createEvent(EventDto eventDto) throws EventValidationException, CategoryNotFoundException, UserNotFoundException, EventDateException;

    List<EventDtoFull> findEventsByUserid(Long userId, int from, int size);

    EventDtoFull findEventByUserId(Long userId, Long eventId) throws EventNotFoundException;

    EventDtoFull updateEventByUserId(EventDto eventDto) throws EventNotFoundException, EventDateException, EventCanceledCantPublishException;

    //Public
    List<EventDtoFull> findEvents(EventFilterPublic eventFilter, HttpServletRequest request) throws FilterValidationException, EventDateException;

    EventDtoFull findEventById(Long eventId, HttpServletRequest request) throws EventNotFoundException;

    Optional<Event> findById(Long eventId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsById(Long eventId);

    List<Event> findAllById(RequestAllEvent requestAllEvent);

    Page<Event> findAllByInitiatorIdIn(RequestAllByInitiatorIds requestAllByInitiatorIds);
}
