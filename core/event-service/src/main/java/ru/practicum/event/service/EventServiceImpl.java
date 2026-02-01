package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ru.practicum.category.Category;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventSpecifications;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.event.feign.CategoryRepository;
import ru.practicum.event.feign.LocationRepository;
import ru.practicum.event.feign.RequestService;
import ru.practicum.event.feign.UserRepository;
import ru.practicum.exception.*;
import ru.practicum.location.Location;
import ru.practicum.request.dto.ConfirmedRequestsCount;
import ru.practicum.stats.ClientRestStat;
import ru.practicum.stats.EndpointHitDto;
import ru.practicum.stats.ViewStatsDto;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ClientRestStat clientRestStat;
    private final EventMapper eventMapper;
    private final Validator validator;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
<<<<<<<< HEAD:core/event-service/src/main/java/ru/practicum/event/service/EventServiceImpl.java
    private final RequestService requestRepository;
========
    private final RequestRepository requestRepository;
>>>>>>>> 7eb8c675e7ba0eaf72df6c1f910a29b9e6f240fe:core/main-service/src/main/java/ru/practicum/mainservice/event/EventServiceImpl.java

    private void validateFilter(EventFilterBase filter) throws FilterValidationException, EventDateException {

        validateDateRange(filter.getRangeStart(), filter.getRangeEnd());

        Errors errors = new BeanPropertyBindingResult(filter, "filter");
        ValidationUtils.invokeValidator(validator, filter, errors);

        if (errors.hasErrors()) {
            String errorMessage = errors.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            throw new FilterValidationException("Filter validation failed: " + errorMessage);
        }
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) throws EventDateException {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new EventDateException("Start date must be before end date");
        }
    }

    private void setDefaultValues(EventDto eventDto) {
        if (eventDto.getPaid() == null) {
            eventDto.setPaid(false);
        }
        if (eventDto.getRequestModeration() == null) {
            eventDto.setRequestModeration(true);
        }
        if (eventDto.getParticipantLimit() == null) {
            eventDto.setParticipantLimit(0);
        }
    }

    private List<EventDtoFull> getDtoFullList(Page<Event> pageEvents) {
        return pageEvents.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    private void addHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        clientRestStat.addStat(endpointHitDto);
    }

    private void enrichEventsWithConfirmedRequests(List<EventDtoFull> events) {
        if (events.isEmpty()) {
            return;
        }
        List<Long> eventIds = events.stream()
                .map(EventDtoFull::getId)
                .collect(Collectors.toList());

        List<ConfirmedRequestsCount> results = requestRepository.findConfirmedRequestsCountByEventIds(eventIds);
        Map<Long, Long> confirmedRequestsMap = results.stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestsCount::getEventId,
                        ConfirmedRequestsCount::getCount
                ));

        events.forEach(event -> {
            Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            event.setConfirmedRequests(confirmedRequests);
        });
    }

    // Получение статистики по списку событий и обогащение views
    private void enrichEventsWithViews(List<EventDtoFull> events, EventFilterAdmin eventFilter) {
        if (events.isEmpty()) {
            return;
        }
        List<Long> eventIds = events.stream()
                .map(EventDtoFull::getId)
                .toList();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        LocalDateTime start = eventFilter.getRangeStart() != null ? eventFilter.getRangeStart() : LocalDateTime.now().minusDays(1);
        LocalDateTime end = eventFilter.getRangeEnd() != null ? eventFilter.getRangeEnd() : LocalDateTime.now().plusDays(1);

        List<ViewStatsDto> stats = clientRestStat.getStat(start, end, uris, true);

        Map<String, Long> viewsByUri = stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));

        events.forEach(event -> {
            String eventUri = "/events/" + event.getId();
            Long views = viewsByUri.getOrDefault(eventUri, 0L);
            event.setViews(views);
        });
    }

    // Admin
    @Override
    public List<EventDtoFull> findEventsByUsers(EventFilterAdmin eventFilter) throws FilterValidationException, EventDateException {
        validateFilter(eventFilter);

        log.info("Main-service. findEventsByUsers input: filter = {}", eventFilter);

        Specification<Event> specification = EventSpecifications.forAdminFilter(eventFilter);

        Pageable pageable = PageRequest.of(eventFilter.getFrom() / eventFilter.getSize(), eventFilter.getSize());

        Page<Event> pageEvents = eventRepository.findAll(specification, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithViews(events, eventFilter);

        log.info("Main-service. findEventsByUsers success: size = {}", events.size());

        return events;
    }

    @Override
    @Transactional
    public EventDtoFull updateEventById(EventDto eventDto) throws EventNotFoundException, EventDateException, EventAlreadyPublishedException, EventCanceledCantPublishException {
        log.info("Main-service. updateEventById input: id = {}", eventDto.getId());
        Event event = eventRepository.findById(eventDto.getId())
                .orElseThrow(() -> new EventNotFoundException("event with id=%d was not found".formatted(eventDto.getId())));

        /*
         * Редактирование данных любого события администратором. Валидация данных не требуется. Обратите внимание:
         * дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
         * событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
         * событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
         */

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new EventDateException("event date should be in 1+ hours after now");
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventAlreadyPublishedException("event is already published");
        }

        if (event.getState().equals(EventState.CANCELED)) {
            throw new EventCanceledCantPublishException("Canceled event cant be published");
        }

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                // прикладной логикой смена флага не управляется, как и в тестах постмана не участвует
                //event.setRequestModeration(false);
            }
            if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }

        eventMapper.updateEventFromDto(eventDto, event);
        Event updatedEvent = eventRepository.save(event);

        log.info("Main-service. updateEventById success: id = {}", updatedEvent.getId());
        return eventMapper.toEventFullDto(updatedEvent);
    }

    // Private
    @Override
    @Transactional
    public EventDtoFull createEvent(EventDto eventDto) throws
            CategoryNotFoundException, UserNotFoundException, EventDateException {
        log.info("Main-service. createEvent input: id = {}", eventDto.getDescription());

        setDefaultValues(eventDto);

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new EventDateException("event date should be in 1+ hours after now");
        }

        Event event = eventMapper.toEvent(eventDto);
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory());
            event.setCategory(category);
        }

        if (eventDto.getInitiator() != null) {
            User user = userRepository.findById(eventDto.getInitiator());
            event.setInitiator(user);
        }

        if (eventDto.getLocation() != null) {
            Location location = locationRepository.findByLatAndLon(
                    eventDto.getLocation().getLat(),
                    eventDto.getLocation().getLon()
            );
            event.setLocation(location);
        }

        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        event.setViews(0L);

        Event createdEvent = eventRepository.save(event);

        log.info("Main-service. createEvent success: id = {}", createdEvent.getId());

        return eventMapper.toEventFullDto(createdEvent);
    }

    @Override
    public EventDtoFull findEventByUserId(Long userId, Long eventId) throws EventNotFoundException {
        log.info("Main-service. findEventByUserId input: userId = {}, eventId = {}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new EventNotFoundException("event with id " + eventId + " not found"));

        log.info("Main-service. findEventByUserId success: id = {}", event.getId());

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventDtoFull> findEventsByUserid(Long userId, int from, int size) {
        log.info("Main-service. findEventsByUserid input: userId = {}, from = {}, size = {}", userId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> pageEvents = eventRepository.findAllByInitiatorId(userId, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        log.info("Main-service. findEventsByUserid success: id = {}", events.size());

        return events;
    }

    @Override
    @Transactional
    public EventDtoFull updateEventByUserId(EventDto eventDto) throws EventNotFoundException, EventDateException, EventCanceledCantPublishException {
        // изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)
        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)

        log.info("Main-service. updateEventByUserId input: eventId = {}, userId = {}", eventDto.getId(), eventDto.getInitiator());

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("event date should be in 2+ hours after now");
        }

        Event existingEvent = eventRepository.findByIdAndInitiatorId(eventDto.getId(), eventDto.getInitiator())
                .orElseThrow(() -> new EventNotFoundException("event with id " + eventDto.getId() + " not found"));

        if (!(existingEvent.getState() == EventState.PENDING || existingEvent.getState() == EventState.CANCELED)) {
            throw new EventCanceledCantPublishException("event can be edited only Pending or Canceled");
        }

        eventMapper.updateEventFromDto(eventDto, existingEvent);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                existingEvent.setState(EventState.PENDING);
            } else {
                existingEvent.setState(EventState.CANCELED);
            }
        }
        Event updatedEvent = eventRepository.save(existingEvent);

        log.info("Main-service. updateEventByUserId success: eventId = {}", updatedEvent.getId());

        return eventMapper.toEventFullDto(updatedEvent);
    }

    //Public
    @Override
    public List<EventDtoFull> findEvents(EventFilterPublic eventFilter, HttpServletRequest request) throws
            FilterValidationException, EventDateException {
        validateFilter(eventFilter);

        log.info("Main-service. findEventsByUsers input: filter = {}", eventFilter);

        // вызов stat-client
        addHit(request);

        Specification<Event> specification = EventSpecifications.forPublicFilter(eventFilter);
        Pageable pageable = PageRequest.of(eventFilter.getFrom() / eventFilter.getSize(), eventFilter.getSize());
        Page<Event> pageEvents = eventRepository.findAll(specification, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        log.info("Main-service. findEventsByUsers success: size = {}", events.size());

        return events;
    }

    @Override
    public EventDtoFull findEventById(Long eventId, HttpServletRequest request) throws EventNotFoundException {
        log.info("Main-service. findEventById input: eventId = {}", eventId);

        // вызов stat-client
        addHit(request);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() -> new EventNotFoundException("event with id " + eventId + " not found"));

        log.info("Main-service. findEventById success: eventId = {}", event.getId());

        EventDtoFull eventDto = eventMapper.toEventFullDto(event);

        // добавляем views из статистики
        enrichEventWithAdditionalData(eventDto);

        return eventDto;
    }

<<<<<<<< HEAD:core/event-service/src/main/java/ru/practicum/event/service/EventServiceImpl.java
    @Override
    public Optional<Event> findById(final Long eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(final Long eventId, final Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId);
    }

    @Override
    public boolean existsByCategoryId(final Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsById(final Long eventId) {
        return eventRepository.existsById(eventId);
    }

    @Override
    public List<Event> findAllById(final Set<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public Page<Event> findAllByInitiatorIdIn(List<Long> userIds, Pageable pageable) {
        return eventRepository.findAllByInitiatorIdIn(
                userIds,
                pageable
        );
    }

    @Override
    public void delete(final Long eventId) {
        eventRepository.deleteById(eventId);
    }

    private void enrichEventWithAdditionalData(EventDtoFull event) {
        Long confirmedRequests = requestRepository.countConfirmedRequests(event.getId());
        log.info("Main-service. enrichEvent: eventId = {}, confirmedRequests = {} ",
                event.getId(), confirmedRequests);
        event.setConfirmedRequests(confirmedRequests);

        String uri = "/events/" + event.getId();

        LocalDateTime start = LocalDateTime.now().minusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        List<ViewStatsDto> stats = clientRestStat.getStat(
                start,
                end,
                List.of(uri),
                true
        );

        log.info("Main-service. enrichEvent: stats response for uri {} = {}", uri, stats);

        Long views = stats.isEmpty() ? 0L : stats.get(0).getHits();
        log.info("Main-service. enrichEvent: eventId = {}, hits = {} ",
                event.getId(), views);
        event.setViews(views);
    }
========
    private void enrichEventWithAdditionalData(EventDtoFull event) {
        Long confirmedRequests = requestRepository.countConfirmedRequests(event.getId());
        log.info("Main-service. enrichEvent: eventId = {}, confirmedRequests = {} ",
                event.getId(), confirmedRequests);
        event.setConfirmedRequests(confirmedRequests);

        String uri = "/events/" + event.getId();

        LocalDateTime start = LocalDateTime.now().minusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        List<ViewStatsDto> stats = clientRestStat.getStat(
                start,
                end,
                List.of(uri),
                true
        );

        log.info("Main-service. enrichEvent: stats response for uri {} = {}", uri, stats);

        Long views = stats.isEmpty() ? 0L : stats.get(0).getHits();
        log.info("Main-service. enrichEvent: eventId = {}, hits = {} ",
                event.getId(), views);
        event.setViews(views);
    }
>>>>>>>> 7eb8c675e7ba0eaf72df6c1f910a29b9e6f240fe:core/main-service/src/main/java/ru/practicum/mainservice/event/EventServiceImpl.java
}