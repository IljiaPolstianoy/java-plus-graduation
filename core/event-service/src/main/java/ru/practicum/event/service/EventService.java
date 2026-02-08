package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.Event;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.filter.EventFilterAdmin;
import ru.practicum.event.filter.EventFilterPublic;
import ru.practicum.exception.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    EventDtoFull findById(Long eventId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsById(Long eventId);

    List<Event> findAllById(Set<Long> ids);

    Page<Event> findAllByInitiatorIdIn(List<Long> ids, Pageable pageable);

    void delete(Long eventId);
}
